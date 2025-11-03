<script lang="ts" setup>
import { type Editor } from "@halo-dev/richtext-editor";
import { Dropdown as VDropdown } from "floating-vue";
import { ref, type Component } from "vue";
import {
  getCurrentGalleryImages,
  updateGalleryImages,
  useAttachmentSelector,
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

const openAttachmentSelector = useAttachmentSelector(
  props.editor,
  (newImages) => {
    const currentImages = getCurrentGalleryImages(props.editor);
    updateGalleryImages(props.editor, [...currentImages, ...newImages]);
    emit("close");
  }
);

const handleAttachmentSelector = () => {
  dropdownShown.value = false;
  openAttachmentSelector();
};

const handleUploadClick = () => {
  dropdownShown.value = false;
  openFileDialog();
};
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
        class="w-48 space-y-1 overflow-hidden rounded-md bg-white p-1 shadow-lg"
      >
        <button
          class="flex w-full items-center rounded px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
          @click="handleUploadClick"
        >
          上传新文件
        </button>
        <button
          class="flex w-full items-center rounded px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
          @click="handleAttachmentSelector"
        >
          从附件库中选择
        </button>
      </div>
    </template>
  </VDropdown>
</template>
