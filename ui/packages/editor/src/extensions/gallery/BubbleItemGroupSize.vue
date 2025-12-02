<script setup lang="ts">
import { BlockActionSeparator } from "@/components";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import {
  IconCheckboxCircle,
  VDropdown,
  VDropdownItem,
  vTooltip,
} from "@halo-dev/components";
import { computed, ref } from "vue";
import IconArrowDownLine from "~icons/ri/arrow-down-s-line";
import { ExtensionGallery } from "./index";

const props = defineProps<BubbleItemComponentProps>();

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
      <IconArrowDownLine class="size-4" />
    </button>

    <template #popper>
      <VDropdownItem
        v-for="option in options"
        :key="option"
        class="!min-w-36"
        @click="handleSetGroupSize(option)"
      >
        {{ option }}

        <template v-if="option === groupSize" #suffix-icon>
          <IconCheckboxCircle class="size-4" />
        </template>
      </VDropdownItem>
    </template>
  </VDropdown>

  <BlockActionSeparator :editor="editor" />
</template>
