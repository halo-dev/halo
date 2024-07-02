<script setup lang="ts">
import {
  BlockActionButton,
  BlockActionInput,
  BlockActionSeparator,
} from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import { computed, type Component } from "vue";
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

function handleSetSize(width?: string, height?: string) {
  props.editor
    .chain()
    .updateAttributes(Image.name, { width, height })
    .setNodeSelection(props.editor.state.selection.from)
    .focus()
    .run();
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
