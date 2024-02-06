<script lang="ts" setup>
import type { Component } from "vue";

const props = withDefaults(
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

const action = () => {
  if (props.disabled) return;
  props.action?.();
};
</script>

<template>
  <div
    :class="[
      { '!bg-gray-100': isActive },
      { 'cursor-not-allowed opacity-70 ': disabled },
      { 'hover:bg-gray-100': !disabled },
    ]"
    class="flex flex-row items-center rounded gap-4 p-1 group cursor-pointer"
    @click="action"
  >
    <component
      :is="icon"
      class="bg-gray-100 p-1 rounded w-6 h-6"
      :class="[
        { '!bg-white': isActive },
        { 'group-hover:bg-white': !disabled },
      ]"
    />
    <span
      class="text-xs text-gray-600"
      :class="[
        { '!text-gray-900 !font-medium': isActive },
        { 'group-hover:font-medium group-hover:text-gray-900': !disabled },
      ]"
    >
      {{ title }}
    </span>
  </div>
</template>
