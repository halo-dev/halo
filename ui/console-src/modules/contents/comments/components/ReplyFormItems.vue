<script setup lang="ts">
import { setFocus } from "@/formkit/utils/focus";
import i18n from "@emoji-mart/data/i18n/zh.json";
import { IconMotionLine, VDropdown } from "@halo-dev/components";
import { Picker } from "emoji-mart";
import { onMounted, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    required?: boolean;
    autoFocus?: boolean;
  }>(),
  {
    required: true,
    autoFocus: true,
  }
);

const emit = defineEmits<{
  (e: "update", value: string): void;
}>();

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

const raw = ref("");

const onEmojiSelect = (emoji: { native: string }) => {
  raw.value += emoji.native;
  setFocus("content-input");
};

onMounted(() => {
  if (props.autoFocus) {
    setFocus("content-input");
  }
});

watch(
  () => raw.value,
  (value) => {
    emit("update", value);
  }
);
</script>
<template>
  <FormKit
    id="content-input"
    v-model="raw"
    type="textarea"
    name="raw"
    :validation-label="$t('core.comment.reply_modal.fields.content.label')"
    :rows="6"
    value=""
    :validation="['length:0,1024', required ? 'required' : ''].join('|')"
  ></FormKit>
  <div class="flex w-full justify-end sm:max-w-lg">
    <VDropdown :classes="['!p-0']" @show="handleCreateEmojiPicker">
      <IconMotionLine
        class="h-5 w-5 cursor-pointer text-gray-500 transition-all hover:text-gray-900"
      />
      <template #popper>
        <div ref="emojiPickerRef"></div>
      </template>
    </VDropdown>
  </div>
</template>
