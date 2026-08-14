<script lang="ts" setup>
import { useHaloKeyboardShortcut } from "@/composables/use-halo-keyboard-shortcut";
import type { ToolbarItemComponentProps } from "@/types";
import { formatShortcut } from "@/utils";
import DropdownItem from "../base/DropdownItem.vue";

type TextTypeLevel = 0 | 1 | 2 | 3 | 4 | 5 | 6;

const props = defineProps<
  ToolbarItemComponentProps & {
    level: TextTypeLevel;
  }
>();

const shortcut = useHaloKeyboardShortcut(props.editor, () => props.shortcutId);

const textTypeClasses: Record<TextTypeLevel, string> = {
  0: "text-base font-normal leading-[30px]",
  1: "text-[28px] font-bold leading-[44.8px]",
  2: "text-2xl font-bold leading-[38.4px]",
  3: "text-xl font-bold leading-8",
  4: "text-base font-bold leading-[25.6px]",
  5: "text-sm font-bold leading-[22.4px]",
  6: "text-sm font-bold leading-[22.4px]",
};

function action() {
  if (props.disabled) {
    return;
  }
  props.action?.();
}
</script>

<template>
  <DropdownItem
    :disabled="disabled"
    :is-active="isActive"
    selection-indicator="leading"
    @click="action"
  >
    <span :class="textTypeClasses[level]">{{ title }}</span>
    <template v-if="shortcut" #suffix>
      <span
        class="flex-none text-xs font-normal text-gray-400/80"
        :aria-label="formatShortcut(shortcut.keys[0])"
      >
        {{ formatShortcut(shortcut.keys[0]) }}
      </span>
    </template>
  </DropdownItem>
</template>
