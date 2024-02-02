<script setup lang="ts">
import { BlockActionInput, BlockActionSeparator } from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import { computed } from "vue";
import Iframe from "./index";

const props = defineProps<{
  editor: Editor;
}>();

const width = computed({
  get: () => {
    return props.editor.getAttributes(Iframe.name).width;
  },
  set: (value: string) => {
    handleSetSize(value, height.value);
  },
});

const height = computed({
  get: () => {
    return props.editor.getAttributes(Iframe.name).height;
  },
  set: (value: string) => {
    handleSetSize(width.value, value);
  },
});

const handleSetSize = (width: string, height: string) => {
  props.editor
    .chain()
    .updateAttributes(Iframe.name, { width, height })
    .focus()
    .setNodeSelection(props.editor.state.selection.from)
    .run();
};
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
</template>
