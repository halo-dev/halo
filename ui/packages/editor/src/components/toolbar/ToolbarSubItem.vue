<script lang="ts" setup>
import { IconCheckboxCircle } from "@halo-dev/components";
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
      { 'cursor-not-allowed opacity-70': disabled },
      { 'hover:bg-gray-100': !disabled },
    ]"
    class="group flex min-h-9 cursor-pointer flex-row items-center gap-3 rounded px-1.5 py-1"
    @click="action"
  >
    <div
      v-if="icon"
      class="size-7 flex-none rounded bg-gray-100 p-1.5"
      :class="{
        'group-hover:bg-white': !disabled,
      }"
    >
      <component :is="icon" class="size-full" />
    </div>

    <div
      class="min-w-0 flex-1 shrink text-sm text-gray-600"
      :class="[
        { '!font-medium !text-gray-900': isActive },
        { 'group-hover:font-medium group-hover:text-gray-900': !disabled },
      ]"
    >
      {{ title }}
    </div>

    <IconCheckboxCircle
      v-if="isActive"
      class="size-4 flex-none text-gray-900"
    />
  </div>
</template>
