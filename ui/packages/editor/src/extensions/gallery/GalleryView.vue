<script lang="ts" setup>
import { i18n } from "@/locales";
import type { NodeViewProps } from "@/tiptap/vue-3";
import { NodeViewWrapper } from "@/tiptap/vue-3";
import { useFileDialog } from "@vueuse/core";
import { computed, ref, watch } from "vue";
import ClarityImageGalleryLine from "~icons/clarity/image-gallery-line";
import ProiconsDelete from "~icons/proicons/delete";

const props = defineProps<NodeViewProps>();

const images = computed({
  get: () => {
    return props.node?.attrs.images || [];
  },
  set: (images: string[]) => {
    props.updateAttributes({
      images: images,
    });
  },
});

const isDragging = ref(false);
const aspectRatios = ref<number[]>([]);

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
  images.value = [...images.value, ...newBlobUrls];
}

function removeImage(index: number) {
  const newImages = [...images.value];
  const removedUrl = newImages[index];

  // 如果是 blob URL，释放它
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
    aspectRatios.value[index] = ratio;
  }
}

function getFlexValue(index: number) {
  const ratio = aspectRatios.value[index];
  if (ratio && ratio > 0) {
    return `${ratio} 1 0%`;
  }
  return "1 1 0%";
}

watch(
  images,
  (newImages, oldImages) => {
    const oldLength = oldImages?.length || 0;
    const newLength = newImages.length;

    if (newLength > oldLength) {
      const newRatios = [...aspectRatios.value];
      for (let i = oldLength; i < newLength; i++) {
        newRatios[i] = 0;
      }
      aspectRatios.value = newRatios;
    } else if (newLength < oldLength) {
      aspectRatios.value = aspectRatios.value.slice(0, newLength);
    } else if (newLength === 0) {
      aspectRatios.value = [];
    }
  },
  { immediate: true }
);

// 对图片进行分组，每 X 个图片为一组，此数值通过 options 参数来调整
const groupSize = computed(() => {
  return props.options?.groupSize || 3;
});

const groups = computed(() => {
  return images.value.reduce(
    (acc: string[][], img: string, index: number): string[][] => {
      const groupIndex = Math.floor(index / groupSize.value);
      acc[groupIndex] = acc[groupIndex] || [];
      acc[groupIndex].push(img);
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
      class="relative"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
    >
      <div
        v-for="(group, groupIndex) in groups"
        :key="groupIndex"
        class="flex flex-row justify-center"
        :class="{ 'mt-2': groupIndex > 0 }"
      >
        <div
          v-for="(src, imgIndex) in group"
          :key="groupIndex * groupSize + imgIndex"
          class="group/image relative pr-2"
          :style="{ flex: getFlexValue(groupIndex * groupSize + imgIndex) }"
        >
          <img
            :src="src"
            :alt="`Gallery image ${groupIndex * groupSize + imgIndex + 1}`"
            class="pointer-events-none block size-full"
            @load="handleImageLoad($event, groupIndex * groupSize + imgIndex)"
          />
          <div
            class="pointer-events-none invisible absolute inset-0 mr-2 bg-gradient-to-t from-black/0 via-black/5 to-black/30 p-3 opacity-0 transition-all group-hover/image:visible group-hover/image:opacity-100"
          >
            <div class="flex flex-row-reverse">
              <button
                aria-label="Delete"
                class="text-grey-900 group pointer-events-auto relative flex h-8 w-9 cursor-pointer items-center justify-center rounded-md bg-white/90 transition-all hover:bg-white hover:text-black"
                type="button"
                @click.stop="removeImage(groupIndex * groupSize + imgIndex)"
              >
                <ProiconsDelete class="h-4 w-4" />
                <div
                  class="text-2xs dark:bg-grey-900 invisible absolute -top-8 left-1/2 z-[1000] flex -translate-x-1/2 items-center gap-1 whitespace-nowrap rounded-md bg-black px-[1rem] py-1 font-sans font-medium text-white group-hover:visible"
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
