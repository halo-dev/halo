<script setup lang="ts">
import { VPopover } from "@halo-dev/components";
import { computed } from "vue";
import MingcuteDistributeSpacingHorizontalLine from "~icons/mingcute/distribute-spacing-horizontal-line";
import { BlockActionSeparator } from "@/components";
import Input from "@/components/base/Input.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { ExtensionGallery } from "./index";

const props = defineProps<BubbleItemComponentProps>();

const gap = computed(() => {
  return props.editor.getAttributes(ExtensionGallery.name).gap;
});

function onGapChange(value: string | number | undefined) {
  props.editor
    .chain()
    .updateAttributes(ExtensionGallery.name, { gap: value })
    .run();
}
</script>

<template>
  <VPopover :offset="10">
    <BubbleButton
      :title="i18n.global.t('editor.extensions.gallery.gap')"
      :text="`${gap}px`"
      show-more-indicator
    >
      <template #icon>
        <MingcuteDistributeSpacingHorizontalLine />
      </template>
    </BubbleButton>

    <template #content>
      <div class="w-80">
        <Input
          auto-focus
          :label="i18n.global.t('editor.extensions.gallery.gap')"
          type="number"
          :model-value="gap"
          @update:model-value="onGapChange"
        />
      </div>
    </template>
  </VPopover>

  <BlockActionSeparator :editor="editor" />
</template>
