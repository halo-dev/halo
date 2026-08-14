<script lang="ts" setup>
import { useHaloKeyboardShortcut } from "@/composables/use-halo-keyboard-shortcut";
import type { ToolbarItemComponentProps } from "@/types";
import { formatShortcut } from "@/utils";
import DropdownItem from "../base/DropdownItem.vue";

const props = withDefaults(
  defineProps<
    ToolbarItemComponentProps & {
      selectionIndicator?: "leading" | "trailing";
    }
  >(),
  {
    selectionIndicator: "leading",
  }
);

const shortcut = useHaloKeyboardShortcut(props.editor, () => props.shortcutId);

const action = () => {
  if (props.disabled) return;
  props.action?.();
};
</script>

<template>
  <DropdownItem
    :disabled="disabled"
    :is-active="isActive"
    :selection-indicator="selectionIndicator"
    @click="action"
  >
    <template v-if="icon" #icon>
      <component :is="icon" />
    </template>
    {{ title }}
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
