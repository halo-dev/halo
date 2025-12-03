<script setup lang="ts">
import { BlockActionInput, BlockActionSeparator } from "@/components";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";
import MdiBackupRestore from "~icons/mdi/backup-restore";
import MdiImageSizeSelectActual from "~icons/mdi/image-size-select-actual";
import MdiImageSizeSelectLarge from "~icons/mdi/image-size-select-large";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import { ExtensionImage } from "./index";

const props = defineProps<BubbleItemComponentProps>();

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

  <BubbleButton
    v-if="visible?.({ editor: props.editor })"
    :title="i18n.global.t('editor.extensions.image.small_size')"
    :is-active="size.width === `25%`"
    @click="handleSetSize({ width: '25%', height: 'auto' })"
  >
    <template #icon>
      <MdiImageSizeSelectSmall class="size-5" />
    </template>
  </BubbleButton>

  <BubbleButton
    v-if="visible?.({ editor: props.editor })"
    :title="i18n.global.t('editor.extensions.image.medium_size')"
    :is-active="size.width === `50%`"
    @click="handleSetSize({ width: '50%', height: 'auto' })"
  >
    <template #icon>
      <MdiImageSizeSelectLarge class="size-5" />
    </template>
  </BubbleButton>

  <BubbleButton
    v-if="visible?.({ editor: props.editor })"
    :title="i18n.global.t('editor.extensions.image.large_size')"
    :is-active="size.width === `100%`"
    @click="handleSetSize({ width: '100%', height: 'auto' })"
  >
    <template #icon>
      <MdiImageSizeSelectActual class="size-5" />
    </template>
  </BubbleButton>

  <BubbleButton
    v-if="visible?.({ editor: props.editor })"
    :title="i18n.global.t('editor.extensions.image.restore_size')"
    @click="handleSetSize({ width: undefined, height: undefined })"
  >
    <template #icon>
      <MdiBackupRestore class="size-5" />
    </template>
  </BubbleButton>

  <BlockActionSeparator
    v-if="visible?.({ editor: props.editor })"
    :editor="editor"
  />
</template>
