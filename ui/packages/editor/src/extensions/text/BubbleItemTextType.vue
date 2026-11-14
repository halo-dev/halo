<script lang="ts" setup>
import { VDropdown } from "@halo-dev/components";
import { computed } from "vue";
import LucideHeading1 from "~icons/lucide/heading-1";
import LucideHeading2 from "~icons/lucide/heading-2";
import LucideHeading3 from "~icons/lucide/heading-3";
import LucideHeading4 from "~icons/lucide/heading-4";
import LucideHeading5 from "~icons/lucide/heading-5";
import LucideHeading6 from "~icons/lucide/heading-6";
import MingcuteParagraphLine from "~icons/mingcute/paragraph-line";
import BubbleButton from "@/components/bubble/BubbleButton.vue";
import ToolbarTextTypeSubItem from "@/components/toolbar/ToolbarTextTypeSubItem.vue";
import { i18n } from "@/locales";
import type { BubbleItemComponentProps } from "@/types";
import { ExtensionHeading } from "../heading";
import { ExtensionParagraph } from "../paragraph";

const props = defineProps<BubbleItemComponentProps>();

const typeOptions = [
  {
    text: i18n.global.t("editor.common.heading.paragraph"),
    icon: MingcuteParagraphLine,
    level: 0 as const,
    shortcutId: "editor.structure.paragraph",
    action: () => props.editor.chain().focus().setParagraph().run(),
    isActive: () => props.editor.isActive(ExtensionParagraph.name),
  },
  {
    text: i18n.global.t("editor.common.heading.heading1"),
    icon: LucideHeading1,
    level: 1 as const,
    shortcutId: "editor.structure.heading1",
    action: () => props.editor.chain().focus().setHeading({ level: 1 }).run(),
    isActive: () => props.editor.isActive(ExtensionHeading.name, { level: 1 }),
  },
  {
    text: i18n.global.t("editor.common.heading.heading2"),
    icon: LucideHeading2,
    level: 2 as const,
    shortcutId: "editor.structure.heading2",
    action: () => props.editor.chain().focus().setHeading({ level: 2 }).run(),
    isActive: () => props.editor.isActive(ExtensionHeading.name, { level: 2 }),
  },
  {
    text: i18n.global.t("editor.common.heading.heading3"),
    icon: LucideHeading3,
    level: 3 as const,
    shortcutId: "editor.structure.heading3",
    action: () => props.editor.chain().focus().setHeading({ level: 3 }).run(),
    isActive: () => props.editor.isActive(ExtensionHeading.name, { level: 3 }),
  },
  {
    text: i18n.global.t("editor.common.heading.heading4"),
    icon: LucideHeading4,
    level: 4 as const,
    shortcutId: "editor.structure.heading4",
    action: () => props.editor.chain().focus().setHeading({ level: 4 }).run(),
    isActive: () => props.editor.isActive(ExtensionHeading.name, { level: 4 }),
  },
  {
    text: i18n.global.t("editor.common.heading.heading5"),
    icon: LucideHeading5,
    level: 5 as const,
    shortcutId: "editor.structure.heading5",
    action: () => props.editor.chain().focus().setHeading({ level: 5 }).run(),
    isActive: () => props.editor.isActive(ExtensionHeading.name, { level: 5 }),
  },
  {
    text: i18n.global.t("editor.common.heading.heading6"),
    icon: LucideHeading6,
    level: 6 as const,
    shortcutId: "editor.structure.heading6",
    action: () => props.editor.chain().focus().setHeading({ level: 6 }).run(),
    isActive: () => props.editor.isActive(ExtensionHeading.name, { level: 6 }),
  },
];

const currentType = computed(() => {
  return typeOptions.find((option) => option.isActive?.());
});
</script>
<template>
  <VDropdown class="inline-flex" :auto-hide="true" :distance="10">
    <BubbleButton
      :title="i18n.global.t('editor.common.heading.title')"
      show-more-indicator
    >
      <template #icon>
        <component :is="currentType?.icon" />
      </template>
    </BubbleButton>

    <template #popper>
      <div class="relative max-h-72 w-56 overflow-hidden overflow-y-auto">
        <ToolbarTextTypeSubItem
          v-for="option in typeOptions"
          :key="option.text"
          v-close-popper
          :editor="editor"
          :is-active="option.isActive?.()"
          :level="option.level"
          :title="option.text"
          :shortcut-id="option.shortcutId"
          :action="option.action"
        />
      </div>
    </template>
  </VDropdown>
</template>
