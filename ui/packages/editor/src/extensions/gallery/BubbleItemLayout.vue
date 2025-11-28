<script setup lang="ts">
import { BlockActionSeparator } from "@/components";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { Dropdown as VDropdown, vTooltip } from "floating-vue";
import { computed, ref, type Component } from "vue";
import IconArrowDownLine from "~icons/ri/arrow-down-s-line";
import { ExtensionGallery } from "./index";

const props = defineProps<{
  editor: Editor;
  isActive?: ({ editor }: { editor: Editor }) => boolean;
  visible?: ({ editor }: { editor: Editor }) => boolean;
  icon?: Component;
  title?: string;
  action?: ({ editor }: { editor: Editor }) => void;
}>();

const dropdownRef = ref();

const layout = computed(() => {
  return props.editor.getAttributes(ExtensionGallery.name).layout || "auto";
});

const options = [
  {
    label: i18n.global.t("editor.extensions.gallery.layout.auto"),
    value: "auto",
  },
  {
    label: i18n.global.t("editor.extensions.gallery.layout.square"),
    value: "square",
  },
];

function handleSetLayout(layout: string) {
  props.editor
    .chain()
    .updateAttributes(ExtensionGallery.name, { layout })
    .run();
  dropdownRef.value?.hide();
}
</script>

<template>
  <VDropdown
    ref="dropdownRef"
    class="inline-flex"
    :triggers="['click']"
    :distance="10"
  >
    <button
      v-tooltip="i18n.global.t('editor.extensions.gallery.layout.title')"
      class="flex items-center gap-1 rounded-md px-1 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
    >
      <span>
        {{ options.find((option) => option.value === layout)?.label }}
      </span>
      <IconArrowDownLine class="h-4 w-4" />
    </button>

    <template #popper>
      <div class="rounded-md bg-white p-1 shadow">
        <div
          v-for="option in options"
          :key="option.value"
          class="flex cursor-pointer items-center justify-center rounded-md px-3 py-2 text-sm hover:bg-gray-100"
          :class="{
            '!bg-gray-200 font-medium': option.value === layout,
          }"
          @click="handleSetLayout(option.value)"
        >
          {{ option.label }}
        </div>
      </div>
    </template>
  </VDropdown>

  <BlockActionSeparator />
</template>
