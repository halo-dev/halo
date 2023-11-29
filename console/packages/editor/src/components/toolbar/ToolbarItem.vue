<script lang="ts" setup>
import type { Component } from "vue";
import { VTooltip } from "floating-vue";

withDefaults(
  defineProps<{
    isActive?: boolean;
    disabled?: boolean;
    title?: string;
    action?: () => void;
    icon?: Component;
  }>(),
  {
    isActive: false,
    disabled: false,
    title: undefined,
    action: undefined,
    icon: undefined,
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
    class="p-1 rounded-sm"
    :disabled="disabled"
    @click="action"
  >
    <component :is="icon" />
  </button>
</template>
