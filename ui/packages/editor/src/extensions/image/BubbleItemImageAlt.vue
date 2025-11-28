<script setup lang="ts">
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { computed, type Component } from "vue";
import { ExtensionImage } from "./index";

const props = defineProps<{
  editor: Editor;
  isActive: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const alt = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionImage.name).alt;
  },
  set: (alt: string) => {
    props.editor
      .chain()
      .updateAttributes(ExtensionImage.name, { alt: alt })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();
  },
});
</script>

<template>
  <input
    v-if="visible?.({ editor: props.editor })"
    v-model.lazy="alt"
    :placeholder="i18n.global.t('editor.common.placeholder.alt_input')"
    class="block w-full rounded-md border !border-solid border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 hover:bg-gray-100 focus:border-blue-500 focus:ring-blue-500"
  />
</template>
