<script setup lang="ts">
import {
  BlockActionButton,
  BlockActionInput,
  BlockActionSeparator,
} from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { computed, type Component } from "vue";
import MdiBackupRestore from "~icons/mdi/backup-restore";
import MdiImageSizeSelectActual from "~icons/mdi/image-size-select-actual";
import MdiImageSizeSelectLarge from "~icons/mdi/image-size-select-large";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import { ExtensionImage } from "./index";

const props = defineProps<{
  editor: Editor;
  isActive?: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const size = computed({
  get: () => {
    return {
      width: props.editor.getAttributes(ExtensionImage.name).width,
      height: props.editor.getAttributes(ExtensionImage.name).height,
    };
  },
  set: (size: { width?: string; height?: string }) => {
    handleSetSize(size);
  },
});

function handleSetSize(size: { width?: string; height?: string }) {
  props.editor
    .chain()
    .updateAttributes(ExtensionImage.name, size)
    .setNodeSelection(props.editor.state.selection.from)
    .focus()
    .run();
}
</script>

<template>
  <BlockActionInput
    v-model.lazy.trim="size.width"
    :visible="visible?.({ editor: props.editor })"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_width_input')"
  />

  <BlockActionInput
    v-model.lazy.trim="size.height"
    :visible="visible?.({ editor: props.editor })"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_height_input')"
  />

  <BlockActionSeparator
    v-if="visible?.({ editor: props.editor })"
    :editor="props.editor"
  />

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.small_size')"
    :is-active="size.width === `25%`"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSize({ width: '25%', height: 'auto' })"
  >
    <template #icon>
      <MdiImageSizeSelectSmall />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.medium_size')"
    :is-active="size.width === `50%`"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSize({ width: '50%', height: 'auto' })"
  >
    <template #icon>
      <MdiImageSizeSelectLarge />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.large_size')"
    :is-active="size.width === `100%`"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSize({ width: '100%', height: 'auto' })"
  >
    <template #icon>
      <MdiImageSizeSelectActual />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="i18n.global.t('editor.extensions.image.restore_size')"
    :visible="visible?.({ editor: props.editor })"
    @click="handleSetSize({ width: undefined, height: undefined })"
  >
    <template #icon>
      <MdiBackupRestore />
    </template>
  </BlockActionButton>

  <BlockActionSeparator v-if="visible?.({ editor: props.editor })" />
</template>
