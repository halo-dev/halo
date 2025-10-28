<script lang="ts" setup>
import { BlockActionInput } from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { computed, onMounted, type Component } from "vue";
import Video, { getVideoElement, handleSetSizePercentage } from "./index";
const props = defineProps<{
  editor: Editor;
  isActive?: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const width = computed({
  get: () => {
    return props.editor.getAttributes(Video.name).width;
  },
  set: (value: string) => {
    handleSetSize(value, height.value);
  },
});

const height = computed({
  get: () => {
    return props.editor.getAttributes(Video.name).height;
  },
  set: (value: string) => {
    handleSetSize(width.value, value);
  },
});

function handleSetSize(width: string, height: string) {
  const newWidth = Math.max(1, parseInt(width));
  const newHeight = Math.max(1, parseInt(height));
  props.editor
    .chain()
    .updateAttributes(Video.name, {
      width: `${newWidth}px`,
      height: `${newHeight}px`,
    })
    .setNodeSelection(props.editor.state.selection.from)
    .focus()
    .run();
}

function convertPercentageToPixels(videoElement: HTMLVideoElement) {
  const attrs = props.editor.getAttributes(Video.name);
  const currentWidth = attrs.width;

  const isWidthPercentage =
    currentWidth &&
    typeof currentWidth === "string" &&
    currentWidth.includes("%");

  if (!isWidthPercentage) {
    return;
  }

  handleSetSizePercentage(props.editor, parseInt(currentWidth), videoElement);
}

onMounted(() => {
  const videoElement = getVideoElement(props.editor);
  if (!videoElement) {
    return;
  }

  if (videoElement.readyState >= 1) {
    convertPercentageToPixels(videoElement);
    return;
  }

  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  const handleLoad = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    convertPercentageToPixels(videoElement);
  };

  const handleError = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
  };

  videoElement.addEventListener("loadedmetadata", handleLoad, {
    once: true,
  });

  videoElement.addEventListener("error", handleError, {
    once: true,
  });

  timeoutId = setTimeout(() => {
    videoElement.removeEventListener("loadedmetadata", handleLoad);
    videoElement.removeEventListener("error", handleError);
  }, 10000);
});
</script>

<template>
  <BlockActionInput
    v-model.lazy.trim="width"
    :visible="visible?.({ editor: props.editor })"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_width_input')"
  />

  <BlockActionInput
    v-model.lazy.trim="height"
    :visible="visible?.({ editor: props.editor })"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_height_input')"
  />
</template>
<style lang="scss">
.editor-block__actions-input {
  @apply block w-32 rounded-md border border-gray-300 bg-gray-50 px-2 py-1 text-sm text-gray-900 hover:bg-gray-100 focus:border-blue-500 focus:ring-blue-500;
}
</style>
