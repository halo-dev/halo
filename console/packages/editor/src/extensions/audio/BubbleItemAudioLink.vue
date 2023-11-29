<script lang="ts" setup>
import type { Editor } from "@/tiptap/vue-3";
import { computed, type Component } from "vue";
import Audio from "./index";
import { i18n } from "@/locales";

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
    return props.editor.getAttributes(Audio.name)?.src;
  },
  set: (src: string) => {
    props.editor
      .chain()
      .updateAttributes(Audio.name, { src: src })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();
  },
});
</script>

<template>
  <input
    v-model.lazy="src"
    :placeholder="i18n.global.t('editor.common.placeholder.link_input')"
    class="bg-gray-50 rounded-md hover:bg-gray-100 block px-2 w-full py-1.5 text-sm text-gray-900 border border-gray-300 focus:ring-blue-500 focus:border-blue-500"
  />
</template>
