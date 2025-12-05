<script setup lang="ts">
import { BlockActionSeparator } from "@/components";
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";
import { ExtensionIframe } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const width = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionIframe.name).width;
  },
  set: (value: string) => {
    handleSetSize(value, height.value);
  },
});

const height = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionIframe.name).height;
  },
  set: (value: string) => {
    handleSetSize(width.value, value);
  },
});

const handleSetSize = (width: string, height: string) => {
  props.editor
    .chain()
    .updateAttributes(ExtensionIframe.name, { width, height })
    .focus()
    .setNodeSelection(props.editor.state.selection.from)
    .run();
};
</script>
<template>
  <Input
    v-model.lazy.trim="width"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_width_input')"
  />

  <Input
    v-model.lazy.trim="height"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_height_input')"
  />

  <BlockActionSeparator :editor="editor" />
</template>
