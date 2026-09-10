<script lang="ts" setup>
import { GetThumbnailByUriSizeEnum } from "@halo-dev/api-client";
import { Toast, VButton, VSpace } from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { computed, ref, toRaw } from "vue";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import { i18n } from "@/locales";
import { NodeViewWrapper, type NodeViewProps } from "@/tiptap";
import {
  DEFAULT_GALLERY_GROUP_SIZE,
  DEFAULT_GALLERY_LAYOUT,
  GALLERY_LAYOUT_SQUARE,
} from "./constants";
import GalleryImageAlt from "./GalleryImageAlt.vue";
import GalleryImageReplace from "./GalleryImageReplace.vue";
import type { ExtensionGalleryImageItem } from "./index";
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

// Keep replacement targets stable across reordering and immutable attribute updates.
const imageIdentities = new WeakMap<
  ExtensionGalleryImageItem,
  ExtensionGalleryImageItem
>();

function imageIdentity(image: ExtensionGalleryImageItem) {
  const rawImage = toRaw(image);
  return imageIdentities.get(rawImage) || rawImage;
}

function updateImage(
  image: ExtensionGalleryImageItem,
  attributes: Partial<ExtensionGalleryImageItem>
) {
  const updated = { ...image, ...attributes };
  imageIdentities.set(updated, imageIdentity(image));
  return updated;
}

function updateImageAlt(index: number, alt: string) {
  images.value = images.value.map(
    (image: ExtensionGalleryImageItem, i: number) =>
      i === index ? updateImage(image, { alt }) : image
  );
}

const pendingUploads = new WeakMap<ExtensionGalleryImageItem, symbol>();

function replaceImage(target: ExtensionGalleryImageItem, src: string) {
  pendingUploads.delete(target);
  const index = images.value.findIndex(
    (image: ExtensionGalleryImageItem) => imageIdentity(image) === target
  );
  const image = images.value[index];
  if (!image || image.src === src) {
    return;
  }
  const newImages = [...images.value];
  newImages[index] = updateImage(image, { src, aspectRatio: 0 });
  images.value = newImages;
}

async function uploadReplacement(image: ExtensionGalleryImageItem, file: File) {
  const target = imageIdentity(image);
  const upload = Symbol();
  pendingUploads.set(target, upload);
  try {
    const attachment = await props.extension.options.uploadImage?.(file);
    const url = attachment?.status?.permalink;
    if (url && pendingUploads.get(target) === upload) {
      replaceImage(target, url);
    }
  } catch (error) {
    Toast.error(
      `${i18n.global.t("editor.extensions.upload.error")} - ${(error as Error).message}`
    );
  }
}

function handleImageLoad(event: Event, index: number) {
  const currentImage = images.value[index];
  if (!currentImage || currentImage.aspectRatio > 0) {
    return;
  }

  const img = event.target as HTMLImageElement;
  if (img.naturalWidth && img.naturalHeight) {
    const ratio = img.naturalWidth / img.naturalHeight;
    const newImages = [...images.value];
    newImages[index] = updateImage(currentImage, { aspectRatio: ratio });
    images.value = newImages;
  }
}

const groupSize = computed<number>(() => {
  return (
    props.node?.attrs.groupSize ||
    props.extension.options?.groupSize ||
    DEFAULT_GALLERY_GROUP_SIZE
  );
});

const layout = computed<string>(() => {
  return props.node?.attrs.layout || DEFAULT_GALLERY_LAYOUT;
});

const gap = computed<number>(() => {
  return props.node?.attrs.gap;
});

const groups = computed<ExtensionGalleryImageItem[][]>(() => {
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

function getThumbnailSize(groupLength: number) {
  if (groupLength === 1) {
    return GetThumbnailByUriSizeEnum.Xl;
  }
  if (groupLength <= 3) {
    return GetThumbnailByUriSizeEnum.M;
  }
  return GetThumbnailByUriSizeEnum.S;
}

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
      return {
        src: url,
        aspectRatio: 0,
      };
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
            'aspect-1': layout === GALLERY_LAYOUT_SQUARE,
          }"
          :style="{
            flex: `${layout === GALLERY_LAYOUT_SQUARE ? '1' : image.aspectRatio} 1 0%`,
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
            :src="
              utils.attachment.getThumbnailUrl(
                image.src,
                getThumbnailSize(group.length)
              )
            "
            :alt="image.alt ?? ''"
            :loading="image.aspectRatio > 0 ? 'lazy' : 'eager'"
            class="pointer-events-none block size-full object-cover"
            @load="handleImageLoad($event, groupIndex * groupSize + imgIndex)"
          />
          <div
            class="group/actions pointer-events-none absolute inset-0 bg-gradient-to-t from-black/0 via-black/5 to-black/30 p-1 opacity-0 transition-all focus-within:opacity-100 group-hover/image:opacity-100 [@media(hover:none)]:opacity-100"
          >
            <div class="flex justify-end gap-1">
              <GalleryImageAlt
                :alt="image.alt ?? ''"
                @update:alt="
                  updateImageAlt(groupIndex * groupSize + imgIndex, $event)
                "
              />
              <GalleryImageReplace
                :upload-enabled="!!extension.options.uploadImage"
                @upload="uploadReplacement(image, $event)"
                @replace="replaceImage(imageIdentity(image), $event)"
              />
              <button
                v-tooltip="
                  i18n.global.t(
                    'editor.extensions.upload.operations.remove.button'
                  )
                "
                aria-label="Delete"
                class="text-grey-900 group pointer-events-none relative flex size-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black active:!bg-white/80 group-focus-within/actions:pointer-events-auto group-hover/image:pointer-events-auto [@media(hover:none)]:pointer-events-auto"
                type="button"
                @click.stop="removeImage(groupIndex * groupSize + imgIndex)"
              >
                <MingcuteDelete2Line class="size-4" />
              </button>
            </div>
          </div>
        </div>
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
