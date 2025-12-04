<script lang="ts" setup>
import Input from "@/components/base/Input.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { computed } from "vue";
import { ExtensionVideo } from "./index";

const props = defineProps<BubbleItemComponentProps>();

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
  <Input
    v-model.lazy.trim="size.width"
    :visible="visible?.({ editor: props.editor })"
    :tooltip="i18n.global.t('editor.common.tooltip.custom_width_input')"
  />

  <Input
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
