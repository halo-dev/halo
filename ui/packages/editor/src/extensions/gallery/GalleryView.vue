<script lang="ts" setup>
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import { i18n } from "@/locales";
import { NodeViewWrapper, type NodeViewProps } from "@/tiptap";
import { Toast, VButton, VSpace } from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { computed, ref } from "vue";
import LucideCaptions from "~icons/lucide/captions";
import CaptionEditor from "./CaptionEditor.vue";
import {
  createGalleryImageItem,
  DEFAULT_GALLERY_GROUP_SIZE,
  MAX_GALLERY_CAPTION_LENGTH,
  type ExtensionGalleryImageItem,
} from "./index";
import { useUploadGalleryImage } from "./useGalleryImages";

const props = defineProps<NodeViewProps>();

const images = computed({
  get: () => {
    return props.node?.attrs.images || [];
  },
  set: (images: ExtensionGalleryImageItem[]) => {
    props.updateAttributes({
      images: images,
    });
  },
});

const { openFileDialog } = useUploadGalleryImage(props.editor);

function handleSetFocus() {
  props.editor.commands.setNodeSelection(props.getPos() || 0);
}

function removeImage(index: number) {
  const newImages = [...images.value];
  newImages.splice(index, 1);
  images.value = newImages;
}

function handleImageLoad(event: Event, index: number) {
  const img = event.target as HTMLImageElement;
  if (img.naturalWidth && img.naturalHeight) {
    const ratio = img.naturalWidth / img.naturalHeight;
    const newImages = [...images.value];
    newImages[index] = {
      ...newImages[index],
      src: img.src,
      aspectRatio: ratio,
    };
    images.value = newImages;
  }
}

function handleImageError() {
  Toast.error(i18n.global.t("editor.extensions.image.image_load_error"));
}

const editingImageIndex = ref<number | null>(null);
const editingImageCaption = ref("");

function openImageCaptionEditor(index: number) {
  handleSetFocus();
  editingImageIndex.value = index;
  editingImageCaption.value = images.value[index]?.caption || "";
}

function closeImageCaptionEditor() {
  editingImageIndex.value = null;
  editingImageCaption.value = "";
}

function saveImageCaption() {
  if (editingImageIndex.value === null) {
    return;
  }
  const index = editingImageIndex.value;
  const newImages = [...images.value];
  const oldImage = newImages[index];
  if (!oldImage) {
    closeImageCaptionEditor();
    return;
  }
  newImages[index] = {
    ...oldImage,
    caption: editingImageCaption.value.trim().slice(0, MAX_GALLERY_CAPTION_LENGTH),
  };
  images.value = newImages;
  handleSetFocus();
  closeImageCaptionEditor();
}

const groupSize = computed(() => {
  return (
    props.node?.attrs.groupSize ||
    props.extension.options?.groupSize ||
    DEFAULT_GALLERY_GROUP_SIZE
  );
});

const layout = computed(() => {
  return props.node?.attrs.layout || "auto";
});

const gap = computed(() => {
  return props.node?.attrs.gap;
});

const galleryCaption = computed({
  get: () => {
    return (props.node?.attrs.caption as string) || "";
  },
  set: (value: string) => {
    props.updateAttributes({
      caption: value.slice(0, MAX_GALLERY_CAPTION_LENGTH),
    });
  },
});

const editingGalleryCaption = ref(false);
const editingGalleryCaptionValue = ref("");

function openGalleryCaptionEditor() {
  handleSetFocus();
  editingGalleryCaption.value = true;
  editingGalleryCaptionValue.value = galleryCaption.value || "";
}

function closeGalleryCaptionEditor() {
  editingGalleryCaption.value = false;
  editingGalleryCaptionValue.value = "";
}

function saveGalleryCaption() {
  galleryCaption.value = editingGalleryCaptionValue.value
    .trim()
    .slice(0, MAX_GALLERY_CAPTION_LENGTH);
  handleSetFocus();
  closeGalleryCaptionEditor();
}

const groups = computed(() => {
  return images.value.reduce(
    (
      acc: ExtensionGalleryImageItem[][],
      image: ExtensionGalleryImageItem,
      index: number
    ) => {
      const groupIndex = Math.floor(index / groupSize.value);
      acc[groupIndex] = acc[groupIndex] || [];
      acc[groupIndex].push(image);
      return acc;
    },
    []
  );
});

const draggedIndex = ref<number | null>(null);
const dragOverIndex = ref<number | null>(null);

function handleDragStart(index: number, event: DragEvent) {
  draggedIndex.value = index;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
    event.dataTransfer.setData("text/html", String(index));
  }
  (event.target as HTMLElement).classList.add("opacity-50");
}

function handleDragEnd(event: DragEvent) {
  (event.target as HTMLElement).classList.remove("opacity-50");
  draggedIndex.value = null;
  dragOverIndex.value = null;
}

function handleDragOver(event: DragEvent) {
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = "move";
  }
}

function handleDragEnter(index: number, event: DragEvent) {
  event.preventDefault();
  dragOverIndex.value = index;
  const target = event.currentTarget as HTMLElement;
  target.classList.add("ring-2", "ring-blue-500");
}

function handleDragLeave(event: DragEvent) {
  const target = event.currentTarget as HTMLElement;
  target.classList.remove("ring-2", "ring-blue-500");
}

function handleDrop(targetIndex: number, event: DragEvent) {
  event.preventDefault();
  event.stopPropagation();

  const target = event.currentTarget as HTMLElement;
  target.classList.remove("ring-2", "ring-blue-500");

  if (draggedIndex.value === null || draggedIndex.value === targetIndex) {
    return;
  }

  const newImages = [...images.value];
  const [movedImage] = newImages.splice(draggedIndex.value, 1);
  newImages.splice(targetIndex, 0, movedImage);
  images.value = newImages;

  draggedIndex.value = null;
  dragOverIndex.value = null;
}

// Attachment Selector Modal
const attachmentSelectorModalVisible = ref(false);

function onAttachmentSelect(attachments: AttachmentLike[]) {
  const newImages = attachments
    .map((attachment) => {
      const url = utils.attachment.getUrl(attachment);
      if (!url) {
        return;
      }
      return createGalleryImageItem(url);
    })
    .filter(Boolean) as ExtensionGalleryImageItem[];
  images.value = [...images.value, ...newImages];
}
</script>

<template>
  <node-view-wrapper
    as="div"
    class="mt-2 p-0.5"
    :class="{
      'rounded ring-2': selected,
    }"
    @click="handleSetFocus"
  >
    <div
      v-if="images.length === 0"
      class="relative flex h-full items-center justify-center rounded-md border border-gray-200 bg-gray-50 before:pb-[62.5%]"
    >
      <VSpace>
        <VButton
          v-if="
            utils.permission.has([
              'uc:attachments:manage',
              'system:attachments:manage',
            ])
          "
          @click="openFileDialog()"
        >
          {{ i18n.global.t("editor.common.button.upload") }}
        </VButton>

        <VButton
          v-if="
            utils.permission.has(
              ['system:attachments:view', 'uc:attachments:manage'],
              true
            )
          "
          @click="attachmentSelectorModalVisible = true"
        >
          {{ i18n.global.t("editor.extensions.upload.attachment.title") }}
        </VButton>
      </VSpace>
    </div>
    <div v-else class="relative grid" :style="{ gap: `${gap}px` }">
      <div
        v-for="(group, groupIndex) in groups"
        :key="groupIndex"
        class="flex flex-row justify-center"
        :style="{ gap: `${gap}px` }"
      >
        <div
          v-for="(image, imgIndex) in group"
          :key="groupIndex * groupSize + imgIndex"
          draggable="true"
          class="group/image relative cursor-grab transition-all active:cursor-grabbing"
          :class="{
            'aspect-1': layout === 'square',
          }"
          :style="{
            flex: `${layout === 'square' ? '1' : image.aspectRatio} 1 0%`,
          }"
          @dragstart="
            handleDragStart(groupIndex * groupSize + imgIndex, $event)
          "
          @dragend="handleDragEnd($event)"
          @dragover="handleDragOver($event)"
          @dragenter="
            handleDragEnter(groupIndex * groupSize + imgIndex, $event)
          "
          @dragleave="handleDragLeave($event)"
          @drop="handleDrop(groupIndex * groupSize + imgIndex, $event)"
        >
          <img
            :src="image.src"
            :alt="image.alt || ''"
            class="pointer-events-none block size-full object-cover"
            @load="handleImageLoad($event, groupIndex * groupSize + imgIndex)"
            @error="handleImageError"
          />
          <div
            class="pointer-events-none invisible absolute inset-0 bg-gradient-to-t from-black/0 via-black/5 to-black/30 p-1 opacity-0 transition-all group-hover/image:visible group-hover/image:opacity-100"
          >
            <div class="flex flex-row-reverse gap-1">
              <button
                v-tooltip="
                  i18n.global.t(
                    'editor.extensions.upload.operations.remove.button'
                  )
                "
                :aria-label="i18n.global.t('editor.common.button.delete')"
                class="text-grey-900 group pointer-events-auto relative flex size-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black active:!bg-white/80"
                type="button"
                @click.stop="removeImage(groupIndex * groupSize + imgIndex)"
              >
                <MingcuteDelete2Line class="size-4" />
              </button>
              <button
                v-tooltip="i18n.global.t('editor.extensions.image.edit_caption')"
                :aria-label="i18n.global.t('editor.extensions.image.edit_caption')"
                class="text-grey-900 group pointer-events-auto relative flex size-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black active:!bg-white/80"
                type="button"
                @click.stop="
                  openImageCaptionEditor(groupIndex * groupSize + imgIndex)
                "
              >
                <LucideCaptions class="size-4" />
              </button>
            </div>
            <div
              v-if="image.caption"
              class="pointer-events-none mt-1 line-clamp-2 rounded bg-black/30 px-1 py-0.5 text-xs text-white"
            >
              {{ image.caption }}
            </div>
          </div>

          <div
            v-if="editingImageIndex === groupIndex * groupSize + imgIndex"
            class="pointer-events-auto absolute inset-x-1 bottom-1 z-10 rounded-md bg-white/95 p-2 shadow ring-1 ring-gray-200"
            @click.stop
          >
            <CaptionEditor
              v-model="editingImageCaption"
              :max-length="MAX_GALLERY_CAPTION_LENGTH"
              :placeholder="
                i18n.global.t('editor.extensions.figure_caption.empty_placeholder')
              "
              :confirm-text="i18n.global.t('editor.common.button.confirm')"
              :cancel-text="i18n.global.t('editor.common.button.cancel')"
              @confirm="saveImageCaption"
              @cancel="closeImageCaptionEditor"
            />
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="selected || galleryCaption"
      class="mt-1 px-1 text-center text-sm text-gray-500"
      @click.stop="openGalleryCaptionEditor"
    >
      <template v-if="!editingGalleryCaption">
        <span v-if="galleryCaption">{{ galleryCaption }}</span>
        <span v-else class="cursor-text text-gray-400">
          {{ i18n.global.t("editor.extensions.figure_caption.empty_placeholder") }}
        </span>
      </template>
      <div v-else class="mx-auto max-w-lg" @click.stop>
        <CaptionEditor
          v-model="editingGalleryCaptionValue"
          :max-length="MAX_GALLERY_CAPTION_LENGTH"
          :placeholder="
            i18n.global.t('editor.extensions.figure_caption.empty_placeholder')
          "
          :confirm-text="i18n.global.t('editor.common.button.confirm')"
          :cancel-text="i18n.global.t('editor.common.button.cancel')"
          @confirm="saveGalleryCaption"
          @cancel="closeGalleryCaptionEditor"
        />
      </div>
    </div>

    <AttachmentSelectorModal
      v-if="attachmentSelectorModalVisible"
      :accepts="['image/*']"
      @select="onAttachmentSelect"
      @close="attachmentSelectorModalVisible = false"
    />
  </node-view-wrapper>
</template>
