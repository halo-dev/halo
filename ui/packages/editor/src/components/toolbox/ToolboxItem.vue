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
    class="group flex cursor-pointer flex-row items-center gap-3 rounded px-1.5 py-1 hover:bg-gray-100"
    @click="action"
  >
    <component
      :is="icon"
      class="h-7 w-7 rounded bg-gray-100 p-1.5 group-hover:bg-white"
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
