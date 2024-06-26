<script lang="ts" setup>
import type { Editor } from "@/tiptap/vue-3";
import type { Component } from "vue";

const props = withDefaults(
  defineProps<{
    editor?: Editor;
    title?: string;
    description?: string;
    action?: () => void;
    icon?: Component;
  }>(),
  {
    editor: undefined,
    title: undefined,
    description: undefined,
    action: undefined,
    icon: undefined,
  }
);

const action = () => {
  props.action?.();
};
</script>

<template>
  <div
    class="flex flex-row items-center rounded gap-3 py-1 px-1.5 group cursor-pointer hover:bg-gray-100"
    @click="action"
  >
    <component
      :is="icon"
      class="bg-gray-100 p-1.5 rounded w-7 h-7 group-hover:bg-white"
    />
    <div class="flex flex-col gap-0.5">
      <span
        class="text-sm text-gray-600 group-hover:font-medium group-hover:text-gray-900"
      >
        {{ title }}
      </span>
      <span v-if="description" class="text-xs text-gray-500">
        {{ description }}
      </span>
    </div>
  </div>
</template>
