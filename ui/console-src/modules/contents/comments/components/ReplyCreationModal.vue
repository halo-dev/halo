<script lang="ts" setup>
import SubmitButton from "@/components/button/SubmitButton.vue";
import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import CommentEditor from "./CommentEditor.vue";

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
const characterCount = ref(0);
const content = ref("");

const handleSubmit = async () => {
  try {
    isSubmitting.value = true;

    await consoleApiClient.content.comment.createReply({
      name: props.comment?.comment.metadata.name as string,
      replyRequest: {
        raw: content.value,
        content: content.value,
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

function onUpdate(value: { content: string; characterCount: number }) {
  content.value = value.content;
  characterCount.value = value.characterCount;
}
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.comment.reply_modal.title')"
    :width="600"
    :mount-to-body="true"
    :centered="false"
    @close="emit('close')"
  >
    <div>
      <CommentEditor :auto-focus="true" @update="onUpdate" />
    </div>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          :disabled="characterCount === 0"
          @submit="handleSubmit"
        >
        </SubmitButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
