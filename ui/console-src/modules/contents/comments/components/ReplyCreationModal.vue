<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import ReplyFormItems from "./ReplyFormItems.vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    comment?: ListedComment;
    reply?: ListedReply;
  }>(),
  {
    visible: false,
    comment: undefined,
    reply: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const isSubmitting = ref(false);

const onSubmit = async (data: { raw: string }) => {
  try {
    isSubmitting.value = true;

    await consoleApiClient.content.comment.createReply({
      name: props.comment?.comment.metadata.name as string,
      replyRequest: {
        raw: data.raw,
        content: data.raw,
        allowNotification: true,
        quoteReply: props.reply?.reply.metadata.name,
      },
    });

    modal.value?.close();

    Toast.success(
      t("core.comment.reply_modal.operations.submit.toast_success")
    );
  } catch (error) {
    console.error("Failed to create comment reply", error);
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.comment.reply_modal.title')"
    :width="500"
    :mount-to-body="true"
    @close="emit('close')"
  >
    <FormKit
      id="create-reply-form"
      name="create-reply-form"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      :classes="{
        form: '!divide-none',
      }"
      @submit="onSubmit"
    >
      <ReplyFormItems />
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('create-reply-form')"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
