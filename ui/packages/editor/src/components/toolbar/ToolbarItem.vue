<script lang="ts" setup>
import MingcuteDownSmallFill from "~icons/mingcute/down-small-fill";
import { useHaloKeyboardShortcuts } from "@/composables/use-halo-keyboard-shortcut";
import type { ToolbarItemComponentProps } from "@/types";
import KeyboardShortcutTooltip from "../keyboard-shortcuts/KeyboardShortcutTooltip.vue";

defineOptions({ inheritAttrs: false });

const props = defineProps<ToolbarItemComponentProps>();

const shortcuts = useHaloKeyboardShortcuts(props.editor, () => {
  if (props.shortcutIds?.length) {
    return props.shortcutIds;
  }
  return props.shortcutId ? [props.shortcutId] : [];
});
</script>

<template>
  <KeyboardShortcutTooltip
    v-slot="tooltipProps"
    :title="title"
    :shortcuts="shortcuts.map((shortcut) => shortcut.keys[0]).filter(Boolean)"
  >
    <button
      v-bind="$attrs"
      type="button"
      :class="[
        { 'bg-gray-200/70': isActive },
        { 'cursor-not-allowed opacity-70': disabled },
        { 'hover:bg-gray-100': !disabled },
        { 'h-8 w-auto px-1.5': !!children?.length },
        { 'size-8': !children?.length },
      ]"
      class="inline-flex items-center justify-center rounded-md p-1 transition-colors active:!bg-gray-200"
      :disabled="disabled"
      tabindex="-1"
      data-editor-toolbar-control
      :aria-label="tooltipProps.ariaLabel"
      :aria-pressed="isActive ? 'true' : undefined"
      :data-state="isActive ? 'on' : 'off'"
      @click="action"
    >
      <component :is="icon" />
      <MingcuteDownSmallFill v-if="children?.length" />
    </button>
  </KeyboardShortcutTooltip>
</template>
