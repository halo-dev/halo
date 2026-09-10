<script setup lang="ts">
import { VDropdown, VDropdownItem } from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { useFileDialog } from "@vueuse/core";
import { computed, ref } from "vue";
import MingcuteRefresh2Line from "~icons/mingcute/refresh-2-line";
import { i18n } from "@/locales";

const props = defineProps<{ uploadEnabled: boolean }>();

const emit = defineEmits<{ replace: [src: string]; upload: [file: File] }>();

const canUpload = computed(
  () =>
    props.uploadEnabled &&
    utils.permission.has(["uc:attachments:manage", "system:attachments:manage"])
);
const canSelect = computed(() =>
  utils.permission.has(["system:attachments:view", "uc:attachments:manage"])
);

const shown = ref(false);

// Upload
const { open: openFileDialog, onChange: onFileDialogChange } = useFileDialog({
  accept: "image/*",
  multiple: false,
  reset: true,
});

onFileDialogChange((files) => {
  const file = files?.[0];
  if (!file) {
    return;
  }
  emit("upload", file);
});

// Attachment Selector Modal
const attachmentSelectorModalVisible = ref(false);

function onAttachmentSelect(attachments: AttachmentLike[]) {
  const url = attachments.length
    ? utils.attachment.getUrl(attachments[0])
    : undefined;
  if (url) {
    emit("replace", url);
  }
  attachmentSelectorModalVisible.value = false;
}
</script>

<template>
  <VDropdown
    v-if="canUpload || canSelect"
    v-model:shown="shown"
    class="pointer-events-none group-focus-within/actions:pointer-events-auto group-hover/image:pointer-events-auto [@media(hover:none)]:pointer-events-auto"
    :triggers="['click']"
    :distance="10"
    @click.stop
    @mousedown.stop
    @dragstart.stop.prevent
  >
    <button
      v-tooltip="
        i18n.global.t('editor.extensions.upload.operations.replace.button')
      "
      type="button"
      :aria-label="
        i18n.global.t('editor.extensions.upload.operations.replace.button')
      "
      :aria-expanded="shown"
      class="text-grey-900 flex size-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black active:!bg-white/80"
    >
      <MingcuteRefresh2Line class="size-4" />
    </button>
    <template #popper>
      <VDropdownItem v-if="canUpload" @click="openFileDialog()">
        {{ i18n.global.t("editor.common.button.upload") }}
      </VDropdownItem>
      <VDropdownItem
        v-if="canSelect"
        @click="attachmentSelectorModalVisible = true"
      >
        {{ i18n.global.t("editor.extensions.upload.attachment.title") }}
      </VDropdownItem>
    </template>
  </VDropdown>
  <AttachmentSelectorModal
    v-if="attachmentSelectorModalVisible"
    :accepts="['image/*']"
    :min="1"
    :max="1"
    @select="onAttachmentSelect"
    @close="attachmentSelectorModalVisible = false"
  />
</template>
