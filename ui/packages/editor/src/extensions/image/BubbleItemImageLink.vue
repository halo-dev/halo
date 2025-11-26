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

const src = computed({
  get: () => {
    return props.editor.getAttributes(ExtensionImage.name).src;
  },
  set: (src: string) => {
    props.editor
      .chain()
      .updateAttributes(ExtensionImage.name, { src: src })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();
  },
});
</script>

<template>
  <input
    v-if="visible?.({ editor: props.editor })"
    v-model.lazy="src"
    :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
    class="block w-full rounded-md border !border-solid border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 hover:bg-gray-100 focus:border-blue-500 focus:ring-blue-500"
  />
</template>
