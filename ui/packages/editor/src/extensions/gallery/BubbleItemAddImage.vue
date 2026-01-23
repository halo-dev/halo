<script lang="ts" setup>
import DropdownItem from "@/components/base/DropdownItem.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { ref } from "vue";
import type { ExtensionGalleryImageItem } from ".";
import {
  getCurrentGalleryImages,
  updateGalleryImages,
  useUploadGalleryImage,
} from "./useGalleryImages";

const props = defineProps<BubbleItemComponentProps>();

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
    <BubbleButton :title="title" :is-active="dropdownShown">
      <template #icon>
        <component :is="icon" :style="iconStyle" />
      </template>
    </BubbleButton>
    <template #popper>
      <DropdownItem
        v-if="
          utils.permission.has([
            'uc:attachments:manage',
            'system:attachments:manage',
          ])
        "
        class="!min-w-36"
        @click="handleUploadClick"
      >
        {{ i18n.global.t("editor.common.button.upload") }}
      </DropdownItem>
      <DropdownItem
        v-if="
          utils.permission.has([
            'system:attachments:view',
            'uc:attachments:manage',
          ])
        "
        class="!min-w-36"
        @click="handleOpenAttachmentSelector"
      >
        {{ i18n.global.t("editor.extensions.upload.attachment.title") }}
      </DropdownItem>
    </template>
  </VDropdown>
  <AttachmentSelectorModal
    v-if="attachmentSelectorModalVisible"
    :accepts="['image/*']"
    @select="onAttachmentSelect"
    @close="attachmentSelectorModalVisible = false"
  />
</template>
