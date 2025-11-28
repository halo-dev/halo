<script lang="ts" setup>
import { i18n } from "@/locales";
import { NodeViewWrapper, type NodeViewProps } from "@/tiptap";
import { VButton, VSpace } from "@halo-dev/components";
import { utils, type AttachmentLike } from "@halo-dev/ui-shared";
import { computed, ref } from "vue";
import ProiconsDelete from "~icons/proicons/delete";
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

function handleImageLoad(event: Event, index: number) {
  const img = event.target as HTMLImageElement;
  if (img.naturalWidth && img.naturalHeight) {
    const ratio = img.naturalWidth / img.naturalHeight;
    const newImages = [...images.value];
    newImages[index] = {
      src: img.src,
      aspectRatio: ratio,
    };
    images.value = [...newImages];
  }
}

const groupSize = computed(() => {
  return props.node?.attrs.groupSize || props.extension.options?.groupSize || 3;
});

const layout = computed(() => {
  return props.node?.attrs.layout || "auto";
});

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
    class="p-0.5"
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
    <div v-else class="relative grid gap-2">
      <div
        v-for="(group, groupIndex) in groups"
        :key="groupIndex"
        class="flex flex-row justify-center gap-2"
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
            :alt="`Gallery image ${groupIndex * groupSize + imgIndex + 1}`"
            class="pointer-events-none block size-full object-cover"
            @load="handleImageLoad($event, groupIndex * groupSize + imgIndex)"
          />
          <div
            class="pointer-events-none invisible absolute inset-0 bg-gradient-to-t from-black/0 via-black/5 to-black/30 p-1 opacity-0 transition-all group-hover/image:visible group-hover/image:opacity-100"
          >
            <div class="flex flex-row-reverse">
              <button
                aria-label="Delete"
                class="text-grey-900 group pointer-events-auto relative flex h-8 w-8 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black"
                type="button"
                @click.stop="removeImage(groupIndex * groupSize + imgIndex)"
              >
                <ProiconsDelete class="h-4 w-4" />
                <div
                  class="text-2xs dark:bg-grey-900 invisible absolute -top-8 left-1/2 z-50 flex -translate-x-1/2 items-center gap-1 whitespace-nowrap rounded-md bg-black px-4 py-1 font-sans font-medium text-white group-hover:visible"
                >
                  <span>
                    {{
                      i18n.global.t(
                        "editor.extensions.upload.operations.remove.button"
                      )
                    }}</span
                  >
                </div>
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
