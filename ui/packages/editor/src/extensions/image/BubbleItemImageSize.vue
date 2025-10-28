<script setup lang="ts">
import {
  BlockActionButton,
  BlockActionInput,
  BlockActionSeparator,
} from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { computed, onMounted, type Component } from "vue";
import MdiBackupRestore from "~icons/mdi/backup-restore";
import MdiImageSizeSelectActual from "~icons/mdi/image-size-select-actual";
import MdiImageSizeSelectLarge from "~icons/mdi/image-size-select-large";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import Image from "./index";

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
    return props.editor.getAttributes(Image.name).width;
  },
  set: (value: string) => {
    handleSetSize(value, height.value);
  },
});

const height = computed({
  get: () => {
    return props.editor.getAttributes(Image.name).height;
  },
  set: (value: string) => {
    handleSetSize(width.value, value);
  },
});

function getImageElement(): HTMLImageElement | null {
  const { view, state } = props.editor;
  const { from } = state.selection;

  let domNode = view.nodeDOM(from);
  if (!domNode && from > 0) {
    const $pos = state.doc.resolve(from);
    if ($pos.parent) {
      domNode = view.domAtPos(from).node as HTMLElement;
    }
  }

  if (domNode instanceof HTMLElement) {
    let img = domNode.querySelector("img");
    if (img) {
      return img;
    }
    if (domNode.tagName === "IMG") {
      return domNode as HTMLImageElement;
    }

    const parent = domNode.parentElement;
    if (parent) {
      img = parent.querySelector("img");
      if (img) {
        return img;
      }
    }
  }

  return null;
}

function handleSetSize(width?: string, height?: string) {
  props.editor
    .chain()
    .updateAttributes(Image.name, { width, height })
    .setNodeSelection(props.editor.state.selection.from)
    .focus()
    .run();
}

function getImageSizePercentage(
  percentage: number
): { width: number; height: number } | undefined {
  const imgElement = getImageElement();

  if (!imgElement || !imgElement.complete) {
    return {
      width: 0,
      height: 0,
    };
  }

  const naturalWidth = imgElement.naturalWidth;
  const naturalHeight = imgElement.naturalHeight;

  if (naturalWidth && naturalHeight) {
    const calculatedWidth = Math.round(naturalWidth * (percentage / 100));
    const calculatedHeight = Math.round(naturalHeight * (percentage / 100));
    return {
      width: calculatedWidth,
      height: calculatedHeight,
    };
  }
}

function handleSetSizeByPercentage(percentage: number) {
  const size = getImageSizePercentage(percentage);
  if (size) {
    handleSetSize(`${size.width}px`, `${size.height}px`);
  }
}

function convertPercentageToPixels() {
  const attrs = props.editor.getAttributes(Image.name);
  const currentWidth = attrs.width;

  const isWidthPercentage =
    currentWidth &&
    typeof currentWidth === "string" &&
    currentWidth.includes("%");

  if (!isWidthPercentage) {
    return;
  }

  handleSetSizeByPercentage(parseInt(currentWidth));
}

onMounted(() => {
  const imgElement = getImageElement();
  if (!imgElement) {
    return;
  }

  if (imgElement.complete) {
    convertPercentageToPixels();
    return;
  }

  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  const handleLoad = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    convertPercentageToPixels();
  };

  const handleError = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
  };

  imgElement.addEventListener("load", handleLoad, {
    once: true,
  });

  imgElement.addEventListener("error", handleError, {
    once: true,
  });

  timeoutId = setTimeout(() => {
    imgElement.removeEventListener("load", handleLoad);
    imgElement.removeEventListener("error", handleError);
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

  <BlockActionSeparator
    v-if="visible?.({ editor: props.editor })"
    :editor="props.editor"
  />

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.small_size')"
    :is-active="width === `${getImageSizePercentage(25)?.width}px`"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSizeByPercentage(25)"
  >
    <template #icon>
      <MdiImageSizeSelectSmall />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.medium_size')"
    :is-active="width === `${getImageSizePercentage(50)?.width}px`"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSizeByPercentage(50)"
  >
    <template #icon>
      <MdiImageSizeSelectLarge />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.large_size')"
    :is-active="width === `${getImageSizePercentage(100)?.width}px`"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSizeByPercentage(100)"
  >
    <template #icon>
      <MdiImageSizeSelectActual />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.restore_size')"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSize(undefined, undefined)"
  >
    <template #icon>
      <MdiBackupRestore />
    </template>
  </BlockActionButton>

  <BlockActionSeparator v-if="visible?.({ editor: props.editor })" />
</template>
