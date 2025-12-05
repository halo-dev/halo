<script setup lang="ts">
import { BlockActionSeparator } from "@/components";
import DropdownItem from "@/components/base/DropdownItem.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { computed, ref } from "vue";
import MingcuteDotGridLine from "~icons/mingcute/dot-grid-line";
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
    <BubbleButton
      :title="i18n.global.t('editor.extensions.gallery.group_size')"
      :text="
        i18n.global.t('editor.extensions.gallery.group_size_label', {
          count: groupSize,
        })
      "
      show-more-indicator
    >
      <template #icon>
        <MingcuteDotGridLine />
      </template>
    </BubbleButton>

    <template #popper>
      <DropdownItem
        v-for="option in options"
        :key="option"
        class="!min-w-36"
        :is-active="option === groupSize"
        @click="handleSetGroupSize(option)"
      >
        {{ option }}
      </DropdownItem>
    </template>
  </VDropdown>

  <BlockActionSeparator :editor="editor" />
</template>
