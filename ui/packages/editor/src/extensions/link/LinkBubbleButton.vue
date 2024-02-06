<script lang="ts" setup>
import { computed, type Component } from "vue";
import { VTooltip, Dropdown as VDropdown } from "floating-vue";
import MdiLinkVariant from "~icons/mdi/link-variant";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";

const props = defineProps<{
  editor: Editor;
  isActive: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const href = computed({
  get() {
    const attrs = props.editor.getAttributes("link");
    return attrs?.href;
  },
  set(value) {
    props.editor.commands.setLink({
      href: value,
      target: target.value ? "_blank" : "_self",
    });
  },
});

const target = computed({
  get() {
    const attrs = props.editor.getAttributes("link");
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
  <VDropdown class="inline-flex" :triggers="['click']" :distance="10">
    <button
      v-tooltip="
        isActive({ editor })
          ? i18n.global.t('editor.extensions.link.edit_link')
          : i18n.global.t('editor.extensions.link.add_link')
      "
      class="text-gray-600 text-lg hover:bg-gray-100 p-2 rounded-md"
      :class="{ 'bg-gray-200 !text-black': isActive({ editor }) }"
    >
      <MdiLinkVariant />
    </button>

    <template #popper>
      <div
        class="relative rounded-md bg-white overflow-hidden drop-shadow w-96 p-1 max-h-72 overflow-y-auto"
      >
        <input
          v-model.lazy="href"
          :placeholder="i18n.global.t('editor.extensions.link.placeholder')"
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
      </div>
    </template>
  </VDropdown>
</template>
