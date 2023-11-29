<script setup lang="ts">
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import { computed, type Component, onUnmounted, ref, watch } from "vue";
import Image from "./index";
import {
  BlockActionButton,
  BlockActionInput,
  BlockActionSeparator,
} from "@/components";
import MdiBackupRestore from "~icons/mdi/backup-restore";
import MdiImageSizeSelectActual from "~icons/mdi/image-size-select-actual";
import MdiImageSizeSelectLarge from "~icons/mdi/image-size-select-large";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import { useResizeObserver } from "@vueuse/core";

const props = defineProps<{
  editor: Editor;
  isActive: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const nodeDom = computed(() => {
  if (!props.editor.isActive(Image.name)) {
    return;
  }
  const nodeDomParent = props.editor.view.nodeDOM(
    props.editor.state.selection.from
  ) as HTMLElement;
  if (nodeDomParent && nodeDomParent.hasChildNodes()) {
    return nodeDomParent.childNodes[0] as HTMLElement;
  }
  return undefined;
});

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

let mounted = false;
const imgScale = ref<number>(0);

watch(nodeDom, () => {
  resetResizeObserver();
});

const reuseResizeObserver = () => {
  let init = true;
  return useResizeObserver(
    nodeDom.value,
    (entries) => {
      // Skip first call
      if (!mounted) {
        mounted = true;
        return;
      }
      const entry = entries[0];
      const { width: w, height: h } = entry.contentRect;
      if (init) {
        imgScale.value = parseFloat((h / w).toFixed(2));
        init = false;
        return;
      }
      const node = props.editor.view.nodeDOM(props.editor.state.selection.from);
      if (!node) {
        return;
      }
      props.editor
        .chain()
        .updateAttributes(Image.name, {
          width: w + "px",
          height: w * imgScale.value + "px",
        })
        .setNodeSelection(props.editor.state.selection.from)
        .focus()
        .run();
    },
    { box: "border-box" }
  );
};

let resizeObserver = reuseResizeObserver();

window.addEventListener("resize", resetResizeObserver);

onUnmounted(() => {
  window.removeEventListener("resize", resetResizeObserver);
});

function resetResizeObserver() {
  resizeObserver.stop();
  resizeObserver = reuseResizeObserver();
}

function handleSetSize(width?: string, height?: string) {
  resizeObserver.stop();
  props.editor
    .chain()
    .updateAttributes(Image.name, { width, height })
    .setNodeSelection(props.editor.state.selection.from)
    .focus()
    .run();
  resizeObserver = reuseResizeObserver();
}
</script>

<template>
  <BlockActionInput
    v-model.lazy.trim="width"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_width_input')"
  />

  <BlockActionInput
    v-model.lazy.trim="height"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_height_input')"
  />

  <BlockActionSeparator />

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.small_size')"
    :selected="editor.getAttributes(Image.name).width === '25%'"
    @click="handleSetSize('25%', 'auto')"
  >
    <template #icon>
      <MdiImageSizeSelectSmall />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.medium_size')"
    :selected="editor.getAttributes(Image.name).width === '50%'"
    @click="handleSetSize('50%', 'auto')"
  >
    <template #icon>
      <MdiImageSizeSelectLarge />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.large_size')"
    :selected="editor.getAttributes(Image.name).width === '100%'"
    @click="handleSetSize('100%', '100%')"
  >
    <template #icon>
      <MdiImageSizeSelectActual />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.restore_size')"
    @click="handleSetSize(undefined, undefined)"
  >
    <template #icon>
      <MdiBackupRestore />
    </template>
  </BlockActionButton>

  <BlockActionSeparator />
</template>
