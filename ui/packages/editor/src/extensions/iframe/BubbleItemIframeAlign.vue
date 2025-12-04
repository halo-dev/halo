<script lang="ts" setup>
import DropdownItem from "@/components/base/DropdownItem.vue";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { VDropdown } from "@halo-dev/components";
import { computed } from "vue";
import MingcuteAlignCenterLine from "~icons/mingcute/align-center-line";
import MingcuteAlignJustifyLine from "~icons/mingcute/align-justify-line";
import MingcuteAlignLeftLine from "~icons/mingcute/align-left-line";
import MingcuteAlignRightLine from "~icons/mingcute/align-right-line";
import { ExtensionParagraph } from "..";

const props = withDefaults(defineProps<BubbleItemComponentProps>(), {
  visible: () => true,
});

const alignOptions = [
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
  {
    text: i18n.global.t("editor.common.align_justify"),
    value: "justify",
    icon: MingcuteAlignJustifyLine,
  },
];

const currentAlign = computed(() => {
  const textAlignAttribute = props.editor.getAttributes(
    ExtensionParagraph.name
  ).textAlign;

  const alignOption = alignOptions.find(
    (option) => option.value === textAlignAttribute
  );

  if (!alignOption) {
    return alignOptions[0];
  }

  return alignOption;
});

const handleSetAlign = (align: string) => {
  props.editor.chain().focus().setTextAlign(align).run();
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
        <component :is="currentAlign.icon" class="size-5" />
      </template>
    </BubbleButton>

    <template #popper>
      <div class="relative max-h-72 w-56 overflow-hidden overflow-y-auto">
        <DropdownItem
          v-for="option in alignOptions"
          :key="option.value"
          v-close-popper
          :is-active="editor.isActive({ textAlign: option.value })"
          @click="handleSetAlign(option.value)"
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
