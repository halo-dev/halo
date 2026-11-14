<script lang="ts" setup>
import { VTooltipComponent } from "@halo-dev/components";
import { computed } from "vue";
import { formatShortcut } from "@/utils";

const props = defineProps<{
  title?: string;
  shortcut?: string;
  shortcuts?: string[];
}>();

function getShortcuts() {
  if (props.shortcuts?.length) {
    return props.shortcuts;
  }
  if (props.shortcut) {
    return [props.shortcut];
  }
  return [];
}

const shortcutText = computed(() => {
  return getShortcuts().map(formatShortcut).join(" / ");
});

const ariaLabel = computed(() => {
  if (!props.title) {
    return undefined;
  }
  if (!shortcutText.value) {
    return props.title;
  }
  return `${props.title}, ${shortcutText.value}`;
});
</script>

<template>
  <VTooltipComponent v-if="title" :distance="8" :delay="{ show: 120, hide: 0 }">
    <slot :aria-label="ariaLabel" />
    <template #popper>
      <div class="flex min-w-12 flex-col items-center gap-1 text-center">
        <span class="text-xs font-normal leading-4 text-white/85">
          {{ title }}
        </span>
        <span
          v-if="shortcutText"
          class="text-xs font-normal leading-4 text-white/85"
          aria-hidden="true"
        >
          {{ shortcutText }}
        </span>
      </div>
    </template>
  </VTooltipComponent>
  <slot v-else :aria-label="undefined" />
</template>
