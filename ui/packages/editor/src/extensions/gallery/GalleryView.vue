<script lang="ts" setup>
import { i18n } from "@/locales";
import type { NodeViewProps } from "@/tiptap/vue-3";
import { NodeViewWrapper } from "@/tiptap/vue-3";
import { useFileDialog } from "@vueuse/core";
import { computed, ref } from "vue";
import ClarityImageGalleryLine from "~icons/clarity/image-gallery-line";
import ProiconsDelete from "~icons/proicons/delete";
import type { GalleryImage } from ".";

const props = defineProps<NodeViewProps>();

const images = computed({
  get: () => {
    return props.node?.attrs.images || [];
  },
  set: (images: GalleryImage[]) => {
    props.updateAttributes({
      images: images,
    });
  },
});

const isDragging = ref(false);

const { open: openFileDialog, onChange } = useFileDialog({
  accept: "image/*",
  multiple: true,
  reset: true,
});

onChange((selectedFiles) => {
  if (selectedFiles) {
    handleFiles(Array.from(selectedFiles));
  }
});

function handleSetFocus() {
  props.editor.commands.setNodeSelection(props.getPos() || 0);
}

function handleFiles(files: File[]) {
  const imageFiles = files.filter((file) => file.type.startsWith("image/"));
  const newBlobUrls = imageFiles.map((file) => URL.createObjectURL(file));
  const newImages = newBlobUrls.map((url) => {
    return {
      src: url,
      aspectRatio: 0,
    };
  });
  images.value = [...images.value, ...newImages];
}

function removeImage(index: number) {
  const newImages = [...images.value];
  const image = newImages[index];
  const removedUrl = image.src;

  if (removedUrl.startsWith("blob:")) {
    URL.revokeObjectURL(removedUrl);
  }

  newImages.splice(index, 1);
  images.value = newImages;
}

function handleDragOver(event: DragEvent) {
  event.preventDefault();
  event.stopPropagation();
  isDragging.value = true;
}

function handleDragLeave(event: DragEvent) {
  event.preventDefault();
  event.stopPropagation();
  isDragging.value = false;
}

function handleDrop(event: DragEvent) {
  event.preventDefault();
  event.stopPropagation();
  isDragging.value = false;

  const files = event.dataTransfer?.files;
  if (files && files.length > 0) {
    handleFiles(Array.from(files));
  }
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

const groups = computed(() => {
  return images.value.reduce(
    (acc: GalleryImage[][], image: GalleryImage, index: number) => {
      const groupIndex = Math.floor(index / groupSize.value);
      acc[groupIndex] = acc[groupIndex] || [];
      acc[groupIndex].push(image);
      return acc;
    },
    []
  );
});
</script>

<template>
  <node-view-wrapper
    as="div"
    :class="{
      'rounded ring-2': selected,
    }"
    @click="handleSetFocus"
  >
    <div
      v-if="images.length === 0"
      class="relative flex h-full items-center justify-center rounded-md border border-gray-200 bg-gray-50 before:pb-[62.5%]"
    >
      <div
        class="group flex cursor-pointer select-none flex-col items-center justify-center p-20"
        @click.stop="openFileDialog()"
        @dragover="handleDragOver"
        @dragleave="handleDragLeave"
        @drop="handleDrop"
      >
        <ClarityImageGalleryLine class="h-16 w-16 text-gray-400" />
        <p
          class="mt-4 flex font-sans text-sm font-normal text-gray-600 opacity-80 transition-all group-hover:opacity-100"
        >
          {{ i18n.global.t("editor.extensions.gallery.empty_prompt") }}
        </p>
      </div>
    </div>
    <div
      v-else
      class="relative grid gap-2"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
    >
      <div
        v-for="(group, groupIndex) in groups"
        :key="groupIndex"
        class="flex flex-row justify-center gap-2"
      >
        <div
          v-for="(image, imgIndex) in group"
          :key="groupIndex * groupSize + imgIndex"
          class="group/image relative"
          :style="{ flex: `${image.aspectRatio} 1 0%` }"
        >
          <img
            :src="image.src"
            :alt="`Gallery image ${groupIndex * groupSize + imgIndex + 1}`"
            class="pointer-events-none block size-full"
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
                  <span>{{
                    i18n.global.t("editor.common.button.delete")
                  }}</span>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </node-view-wrapper>
</template>
