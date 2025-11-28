<script lang="ts" setup>
import { BlockActionInput } from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { computed, type Component } from "vue";
import { ExtensionVideo } from "./index";
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
      width: props.editor.getAttributes(ExtensionVideo.name).width,
      height: props.editor.getAttributes(ExtensionVideo.name).height,
    };
  },
  set: (size: { width?: string; height?: string }) => {
    handleSetSize(size);
  },
});

function handleSetSize(size: { width?: string; height?: string }) {
  props.editor
    .chain()
    .updateAttributes(ExtensionVideo.name, size)
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
</template>
<style lang="scss">
.editor-block__actions-input {
  @apply block w-32 rounded-md border border-gray-300 bg-gray-50 px-2 py-1 text-sm text-gray-900 hover:bg-gray-100 focus:border-blue-500 focus:ring-blue-500;
}
</style>
