<script lang="ts" setup>
import { i18n } from "@/locales";
import { formatShortcut, formatShortcutParts } from "@/utils";

withDefaults(
  defineProps<{
    shortcuts: string[];
    compact?: boolean;
  }>(),
  {
    compact: false,
  }
);
</script>

<template>
  <span
    class="inline-flex flex-wrap items-center justify-end gap-1"
    :aria-label="shortcuts.map(formatShortcut).join(' / ')"
  >
    <template v-for="(shortcut, shortcutIndex) in shortcuts" :key="shortcut">
      <span
        v-if="shortcutIndex"
        aria-hidden="true"
        class="px-0.5 text-[10px] text-gray-400"
      >
        {{ i18n.global.t("editor.shortcuts.or") }}
      </span>
      <kbd
        v-for="part in formatShortcutParts(shortcut)"
        :key="`${shortcut}-${part}`"
        class="inline-flex items-center justify-center border border-gray-200 bg-gray-50 font-sans font-medium text-gray-600 shadow-[0_1px_0_rgba(0,0,0,0.06)]"
        :class="
          compact
            ? 'min-w-5 rounded px-1 py-0 text-[10px] leading-4'
            : 'min-w-6 rounded-md px-1.5 py-0.5 text-[11px] leading-5'
        "
      >
        {{ part }}
      </kbd>
    </template>
  </span>
</template>
