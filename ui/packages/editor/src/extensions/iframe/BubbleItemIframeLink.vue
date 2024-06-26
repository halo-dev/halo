<script setup lang="ts">
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import { isAllowedUri } from "@/utils/is-allowed-uri";
import { computed, type Component } from "vue";
import Iframe from "./index";

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
    return props.editor.getAttributes(Iframe.name).src;
  },
  set: (src: string) => {
    if (!src || !isAllowedUri(src)) {
      return;
    }
    props.editor.chain().updateAttributes(Iframe.name, { src: src }).run();
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
