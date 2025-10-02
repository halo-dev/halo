<script setup lang="ts">
import { IconArrowLeft, IconArrowRight } from "@halo-dev/components";

defineProps<{
  index: number;
  total: number;
}>();

const emit = defineEmits<{
  (e: "prev"): void;
  (e: "next"): void;
  (e: "update:index", index: number): void;
}>();
</script>

<template>
  <div
    class="flex items-center gap-4 rounded-full bg-white p-1 transition-all hover:shadow"
  >
    <button
      v-if="total > 1"
      class="group flex h-7 w-7 items-center justify-center rounded-full bg-transparent transition-all duration-200 hover:bg-gray-100 focus:outline-none"
      @click="emit('prev')"
    >
      <IconArrowLeft class="text-gray-400 group-hover:text-gray-900" />
    </button>

    <div class="flex items-center gap-2">
      <div
        v-for="i in total"
        :key="i"
        class="group cursor-pointer"
        @click="emit('update:index', i - 1)"
      >
        <div
          class="h-2 w-2 transform rounded-full transition-all duration-200 ease-in-out"
          :class="{
            'scale-150 bg-primary': index === i - 1,
            'bg-gray-300 group-hover:bg-gray-400': index !== i - 1,
          }"
        />
      </div>
    </div>

    <button
      v-if="total > 1"
      class="group flex h-7 w-7 items-center justify-center rounded-full bg-transparent transition-all duration-200 hover:bg-gray-100 focus:outline-none"
      @click="emit('next')"
    >
      <IconArrowRight class="text-gray-400 group-hover:text-gray-900" />
    </button>
  </div>
</template>
