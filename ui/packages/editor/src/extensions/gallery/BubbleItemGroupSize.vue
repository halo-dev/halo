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

const groupSize = computed(() => {
  return props.editor.getAttributes(ExtensionGallery.name).groupSize || 3;
});

const options = [1, 2, 3, 4, 5, 6];

function handleSetGroupSize(size: number) {
  const currentImages =
    props.editor.getAttributes(ExtensionGallery.name).images || [];

  props.editor
    .chain()
    .updateAttributes(ExtensionGallery.name, {
      images: currentImages,
      groupSize: size,
    })
    .setNodeSelection(props.editor.state.selection.from)
    .focus()
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
      v-tooltip="i18n.global.t('editor.extensions.gallery.group_size')"
      class="flex items-center gap-1 rounded-md px-1 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
    >
      <span>{{ groupSize }}</span>
      <IconArrowDownLine class="h-4 w-4" />
    </button>

    <template #popper>
      <div class="w-16 rounded-md bg-white p-1 shadow">
        <div
          v-for="option in options"
          :key="option"
          class="flex cursor-pointer items-center justify-center rounded-md px-3 py-2 text-sm hover:bg-gray-100"
          :class="{
            '!bg-gray-200 font-medium': option === groupSize,
          }"
          @click="handleSetGroupSize(option)"
        >
          {{ option }}
        </div>
      </div>
    </template>
  </VDropdown>

  <BlockActionSeparator />
</template>
