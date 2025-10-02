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
      { 'cursor-not-allowed opacity-70': disabled },
      { 'hover:bg-gray-100': !disabled },
    ]"
    class="group flex cursor-pointer flex-row items-center gap-3 rounded px-1.5 py-1"
    @click="action"
  >
    <component
      :is="icon"
      class="h-7 w-7 rounded bg-gray-100 p-1.5"
      :class="[
        { '!bg-white': isActive },
        { 'group-hover:bg-white': !disabled },
      ]"
    />
    <span
      class="text-sm text-gray-600"
      :class="[
        { '!font-medium !text-gray-900': isActive },
        { 'group-hover:font-medium group-hover:text-gray-900': !disabled },
      ]"
    >
      {{ title }}
    </span>
  </div>
</template>
