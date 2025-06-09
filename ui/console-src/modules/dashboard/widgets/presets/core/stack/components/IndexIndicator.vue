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
    class="bg-white rounded-full p-1 flex gap-4 items-center hover:shadow transition-all"
  >
    <button
      v-if="total > 1"
      class="w-7 h-7 flex items-center justify-center rounded-full bg-transparent hover:bg-gray-100 transition-all duration-200 focus:outline-none group"
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
          class="w-2 h-2 rounded-full transition-all duration-200 ease-in-out transform"
          :class="{
            'bg-primary scale-150': index === i - 1,
            'bg-gray-300 group-hover:bg-gray-400': index !== i - 1,
          }"
        />
      </div>
    </div>

    <button
      v-if="total > 1"
      class="w-7 h-7 flex items-center justify-center rounded-full bg-transparent hover:bg-gray-100 transition-all duration-200 focus:outline-none group"
      @click="emit('next')"
    >
      <IconArrowRight class="text-gray-400 group-hover:text-gray-900" />
    </button>
  </div>
</template>
