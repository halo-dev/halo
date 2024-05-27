<script lang="ts" setup>
import {
  IconMotionLine,
  Toast,
  VButton,
  VDropdown,
  VModal,
  VSpace,
} from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import type {
  ListedComment,
  ListedReply,
  ReplyRequest,
} from "@halo-dev/api-client";
// @ts-ignore
import { Picker } from "emoji-mart";
import i18n from "@emoji-mart/data/i18n/zh.json";
import { onMounted, ref } from "vue";
import { setFocus } from "@/formkit/utils/focus";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

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
const formState = ref<ReplyRequest>({
  raw: "",
  content: "",
  allowNotification: true,
  quoteReply: undefined,
});
const saving = ref(false);

onMounted(() => {
  setFocus("content-input");
});

const handleCreateReply = async () => {
  try {
    saving.value = true;

    if (props.reply) {
      formState.value.quoteReply = props.reply.reply.metadata.name;
    }

    formState.value.content = formState.value.raw;

    await apiClient.comment.createReply({
      name: props.comment?.comment.metadata.name as string,
      replyRequest: formState.value,
    });

    modal.value?.close();

    Toast.success(
      t("core.comment.reply_modal.operations.submit.toast_success")
    );
  } catch (error) {
    console.error("Failed to create comment reply", error);
  } finally {
    saving.value = false;
  }
};

// Emoji picker
const emojiPickerRef = ref<HTMLElement | null>(null);

const handleCreateEmojiPicker = async () => {
  if (emojiPickerRef.value?.childElementCount) {
    return;
  }

  const data = await import("@emoji-mart/data");

  const emojiPicker = new Picker({
    data: Object.assign({}, data),
    theme: "light",
    autoFocus: true,
    i18n: i18n,
    onEmojiSelect: onEmojiSelect,
  });

  emojiPickerRef.value?.appendChild(emojiPicker as unknown as Node);
};

const onEmojiSelect = (emoji: { native: string }) => {
  formState.value.raw += emoji.native;
  setFocus("content-input");
};
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.comment.reply_modal.title')"
    :width="500"
    @close="emit('close')"
  >
    <FormKit
      id="create-reply-form"
      name="create-reply-form"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleCreateReply"
    >
      <FormKit
        id="content-input"
        v-model="formState.raw"
        type="textarea"
        :validation-label="$t('core.comment.reply_modal.fields.content.label')"
        :rows="6"
        value=""
        validation="required|length:0,1024"
      ></FormKit>
    </FormKit>
    <div class="mt-2 flex justify-end">
      <VDropdown :classes="['!p-0']" @show="handleCreateEmojiPicker">
        <IconMotionLine
          class="h-5 w-5 cursor-pointer text-gray-500 transition-all hover:text-gray-900"
        />
        <template #popper>
          <div ref="emojiPickerRef"></div>
        </template>
      </VDropdown>
    </div>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="saving"
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
