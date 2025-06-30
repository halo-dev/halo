<script lang="ts" setup>
import type { ToolbarItemType } from "@/types";
import { vTooltip } from "floating-vue";
import type { Component } from "vue";
import MdiMenuDown from "~icons/mdi/menu-down";

withDefaults(
  defineProps<{
    isActive?: boolean;
    disabled?: boolean;
    title?: string;
    action?: () => void;
    icon?: Component;
    children?: ToolbarItemType[];
  }>(),
  {
    isActive: false,
    disabled: false,
    title: undefined,
    action: undefined,
    icon: undefined,
    children: undefined,
  }
);
</script>

<template>
  <button
    v-tooltip="title"
    :class="[
      { 'bg-gray-200/70': isActive },
      { 'cursor-not-allowed opacity-70': disabled },
      { 'hover:bg-gray-100': !disabled },
    ]"
    class="inline-flex items-center space-x-1 rounded-md p-1.5 active:bg-gray-200"
    :disabled="disabled"
    tabindex="-1"
    @click="action"
  >
    <component :is="icon" />
    <MdiMenuDown v-if="children?.length" />
  </button>
</template>
