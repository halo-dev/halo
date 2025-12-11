<script lang="ts" setup>
import type { ToolbarItemComponentProps } from "@/types";
import { vTooltip } from "@halo-dev/components";
import MingcuteDownSmallFill from "~icons/mingcute/down-small-fill";

defineProps<ToolbarItemComponentProps>();
</script>

<template>
  <button
    v-tooltip="title"
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
    @click="action"
  >
    <component :is="icon" />
    <MingcuteDownSmallFill v-if="children?.length" />
  </button>
</template>
