<script lang="ts" setup>
import { ref } from "vue";

defineProps<{
  searchable: boolean;
}>();

const emit = defineEmits<{
  (event: "search", value: string, e?: Event): void;
  (event: "enter", value: string): void;
}>();

const inputHTMLRef = ref<HTMLInputElement | null>(null);
const searchInputContainerRef = ref<HTMLSpanElement | null>(null);
const inputValue = ref("");
</script>

<template>
  <div class="relative w-full cursor-text">
    <span ref="searchInputContainerRef" class="relative flex w-full">
      <input
        ref="inputHTMLRef"
        v-model="inputValue"
        type="text"
        :readonly="!searchable"
        autocomplete="off"
        class="m-0 h-full w-full cursor-auto !appearance-none border-none bg-transparent p-0 pe-0 ps-0 text-base outline-none"
        :class="{
          '!cursor-pointer': !searchable,
        }"
        @input="(event) => emit('search', inputValue, event)"
      />
    </span>
    <span
      class="pointer-events-none absolute inset-y-0 left-0 w-full truncate text-sm"
    >
      <slot name="placeholder"></slot>
    </span>
  </div>
</template>
