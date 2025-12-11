<script lang="ts" setup>
import DropdownItem from "@/components/base/DropdownItem.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { computed } from "vue";
import MingcuteAlignCenterLine from "~icons/mingcute/align-center-line";
import MingcuteAlignLeftLine from "~icons/mingcute/align-left-line";
import MingcuteAlignRightLine from "~icons/mingcute/align-right-line";
import { ExtensionFigure, ExtensionImage } from "..";

const props = withDefaults(defineProps<BubbleItemComponentProps>(), {
  visible: () => true,
});

const positionOptions = [
  {
    text: i18n.global.t("editor.common.align_left"),
    value: "left",
    icon: MingcuteAlignLeftLine,
  },
  {
    text: i18n.global.t("editor.common.align_center"),
    value: "center",
    icon: MingcuteAlignCenterLine,
  },
  {
    text: i18n.global.t("editor.common.align_right"),
    value: "right",
    icon: MingcuteAlignRightLine,
  },
];

const currentPosition = computed(() => {
  const positionAttribute = props.editor.getAttributes(
    ExtensionImage.name
  ).position;

  const positionOption = positionOptions.find(
    (option) => option.value === positionAttribute
  );

  if (!positionOption) {
    return positionOptions[0];
  }

  return positionOption;
});

const handleSetPosition = (position: string) => {
  props.editor
    .chain()
    .focus()
    .updateAttributes(ExtensionImage.name, { position })
    .updateAttributes(ExtensionFigure.name, { position })
    .run();
};
</script>
<template>
  <VDropdown
    v-if="visible({ editor })"
    class="inline-flex"
    :auto-hide="true"
    :distance="10"
  >
    <BubbleButton
      :title="i18n.global.t('editor.common.align_method')"
      show-more-indicator
    >
      <template #icon>
        <component :is="currentPosition.icon" />
      </template>
    </BubbleButton>

    <template #popper>
      <div class="relative max-h-72 w-56 overflow-hidden overflow-y-auto">
        <DropdownItem
          v-for="option in positionOptions"
          :key="option.value"
          v-close-popper
          :is-active="editor.isActive({ position: option.value })"
          @click="handleSetPosition(option.value)"
        >
          <template #icon>
            <component :is="option.icon" />
          </template>
          {{ option.text }}
        </DropdownItem>
      </div>
    </template>
  </VDropdown>
</template>
