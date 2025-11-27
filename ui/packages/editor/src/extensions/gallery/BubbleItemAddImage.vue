<script lang="ts" setup>
import { i18n } from "@/locales";
import { type Editor } from "@/tiptap";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { Dropdown as VDropdown } from "floating-vue";
import { ref, type Component } from "vue";
import type { ExtensionGalleryImageItem } from ".";
import {
  getCurrentGalleryImages,
  updateGalleryImages,
  useUploadGalleryImage,
} from "./useGalleryImages";

const props = defineProps<{
  editor: Editor;
  visible: ({ editor }: { editor: Editor }) => boolean;
  isActive: ({ editor }: { editor: Editor }) => boolean;
  title: string;
  icon: Component;
  iconStyle: string;
  action: ({ editor }: { editor: Editor }) => void;
}>();
const emit = defineEmits(["close"]);

const dropdownShown = ref(false);
const { openFileDialog } = useUploadGalleryImage(props.editor);

const handleUploadClick = () => {
  dropdownShown.value = false;
  openFileDialog();
};

// Attachment Selector Modal
const attachmentSelectorModalVisible = ref(false);

const handleOpenAttachmentSelector = () => {
  dropdownShown.value = false;
  attachmentSelectorModalVisible.value = true;
};

function onAttachmentSelect(attachments: AttachmentLike[]) {
  const currentImages = getCurrentGalleryImages(props.editor);
  const newImages = attachments
    .map((attachment) => {
      const url = utils.attachment.getUrl(attachment);
      if (!url) {
        return;
      }
      return {
        src: url,
        aspectRatio: 0,
      };
    })
    .filter(Boolean) as ExtensionGalleryImageItem[];
  updateGalleryImages(props.editor, [...currentImages, ...newImages]);
  emit("close");
}
</script>

<template>
  <VDropdown v-model:shown="dropdownShown" :triggers="['click']" :distance="10">
    <button
      :title="title"
      class="rounded-md p-2 text-lg text-gray-600 hover:bg-gray-100"
      :class="{ 'bg-gray-200': dropdownShown }"
    >
      <component :is="icon" :style="iconStyle" class="h-5 w-5" />
    </button>
    <template #popper>
      <div
        class="w-24 space-y-1 overflow-hidden rounded-md bg-white p-1 shadow-lg"
      >
        <button
          v-if="
            utils.permission.has([
              'uc:attachments:manage',
              'system:attachments:manage',
            ])
          "
          class="flex w-full items-center rounded px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
          @click="handleUploadClick"
        >
          {{ i18n.global.t("editor.common.button.upload") }}
        </button>
        <button
          v-if="
            utils.permission.has([
              'system:attachments:view',
              'uc:attachments:manage',
            ])
          "
          class="flex w-full items-center rounded px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
          @click="handleOpenAttachmentSelector"
        >
          {{ i18n.global.t("editor.extensions.upload.attachment.title") }}
        </button>
      </div>
    </template>
  </VDropdown>
  <AttachmentSelectorModal
    v-if="attachmentSelectorModalVisible"
    :accepts="['image/*']"
    @select="onAttachmentSelect"
    @close="attachmentSelectorModalVisible = false"
  />
</template>
