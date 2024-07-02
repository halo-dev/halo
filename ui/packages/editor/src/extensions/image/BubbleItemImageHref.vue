<script setup lang="ts">
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import { computed, type Component } from "vue";
import { ExtensionLink, ExtensionImage } from "@/extensions";

const props = defineProps<{
  editor: Editor;
  isActive: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const href = computed({
  get: () => {
    const attrs = props.editor.getAttributes(ExtensionLink.name);
    return attrs?.href || props.editor.getAttributes(ExtensionImage.name).href;
  },
  set: (href: string) => {
    props.editor.commands.setLink({ href: href, target: "_blank" });
  },
});

const target = computed({
  get() {
    const attrs = props.editor.getAttributes(ExtensionLink.name);
    return attrs?.target === "_blank";
  },
  set(value) {
    props.editor.commands.setLink({
      href: href.value,
      target: value ? "_blank" : "_self",
    });
  },
});
</script>

<template>
  <input
    v-model.lazy="href"
    :placeholder="i18n.global.t('editor.common.placeholder.alt_href')"
    class="bg-gray-50 rounded-md hover:bg-gray-100 block px-2 w-full py-1.5 text-sm text-gray-900 border border-gray-300 focus:ring-blue-500 focus:border-blue-500"
  />
  <label class="inline-flex items-center mt-2">
    <input
      v-model="target"
      type="checkbox"
      class="form-checkbox text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
    />
    <span class="ml-2 text-sm text-gray-500">
      {{ i18n.global.t("editor.extensions.link.open_in_new_window") }}
    </span>
  </label>
</template>
