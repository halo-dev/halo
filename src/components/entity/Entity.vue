<script lang="ts" setup>
import { IconMore, VSpace } from "@halo-dev/components";
withDefaults(
  defineProps<{
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);
</script>

<template>
  <div
    :class="{
      'bg-gray-100': isSelected,
    }"
    class="group relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
  >
    <div
      v-show="isSelected"
      class="absolute inset-y-0 left-0 w-0.5 bg-primary"
    ></div>
    <slot name="prepend" />
    <div class="relative flex flex-row items-center">
      <div v-if="$slots.checkbox" class="mr-4 hidden items-center sm:flex">
        <slot name="checkbox" />
      </div>
      <div class="flex flex-1 items-center gap-4">
        <slot name="start" />
      </div>
      <div
        class="flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
      >
        <slot name="end" />
      </div>
      <div v-if="$slots.menuItems" class="ml-4 inline-flex items-center">
        <FloatingDropdown>
          <div
            class="cursor-pointer rounded p-1 transition-all hover:text-blue-600 group-hover:bg-gray-100"
          >
            <IconMore />
          </div>
          <template #popper>
            <div class="w-48 p-2">
              <VSpace class="w-full" direction="column">
                <slot name="menuItems"></slot>
              </VSpace>
            </div>
          </template>
        </FloatingDropdown>
      </div>
    </div>
  </div>
</template>
