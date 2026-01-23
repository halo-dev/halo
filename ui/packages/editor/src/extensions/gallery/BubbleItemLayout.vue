<script setup lang="ts">
import { BlockActionSeparator } from "@/components";
import DropdownItem from "@/components/base/DropdownItem.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { computed, ref } from "vue";
import MingcuteLayout10Line from "~icons/mingcute/layout-10-line";
import MingcuteLayoutGridLine from "~icons/mingcute/layout-grid-line";
import { ExtensionGallery } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const dropdownRef = ref();

const layout = computed(() => {
  return props.editor.getAttributes(ExtensionGallery.name).layout || "auto";
});

const options = [
  {
    label: i18n.global.t("editor.extensions.gallery.layout.auto"),
    value: "auto",
    icon: MingcuteLayout10Line,
  },
  {
    label: i18n.global.t("editor.extensions.gallery.layout.square"),
    value: "square",
    icon: MingcuteLayoutGridLine,
  },
];

const currentLayout = computed(() => {
  return options.find((option) => option.value === layout.value) || options[0];
});

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
    <BubbleButton
      :title="i18n.global.t('editor.extensions.gallery.layout.title')"
      :text="currentLayout.label"
      show-more-indicator
    >
      <template #icon>
        <component :is="currentLayout.icon" />
      </template>
    </BubbleButton>

    <template #popper>
      <DropdownItem
        v-for="option in options"
        :key="option.value"
        class="!min-w-36"
        :is-active="option.value === layout"
        @click="handleSetLayout(option.value)"
      >
        <template #icon>
          <component :is="option.icon" />
        </template>
        {{ option.label }}
      </DropdownItem>
    </template>
  </VDropdown>

  <BlockActionSeparator :editor="editor" />
</template>
