<script lang="ts" setup>
import type { Component } from "vue";
import IconArrowDown from "~icons/ri/arrow-down-s-fill";
import type { ToolbarItem } from "@/types";

withDefaults(
  defineProps<{
    isActive?: boolean;
    disabled?: boolean;
    title?: string;
    action?: () => void;
    icon?: Component;
    children: ToolbarItem[];
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
      { 'bg-gray-200': isActive },
      { 'cursor-not-allowed opacity-70': disabled },
      { 'hover:bg-gray-100': !disabled },
    ]"
    class="inline-flex items-center space-x-1 p-1 rounded-sm"
    :disabled="disabled"
    tabindex="-1"
    @click="action"
  >
    <component :is="icon" />
    <IconArrowDown v-if="children?.length" />
  </button>
</template>
