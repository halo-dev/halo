<script lang="ts" setup>
import { BlockActionInput } from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import { computed, type Component } from "vue";
import Video from "./index";
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
  props.editor
    .chain()
    .updateAttributes(Video.name, { width, height })
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
</template>
<style lang="scss">
.editor-block__actions-input {
  @apply bg-gray-50 rounded-md hover:bg-gray-100 block px-2 w-32 py-1 text-sm text-gray-900 border border-gray-300  focus:ring-blue-500 focus:border-blue-500;
}
</style>
