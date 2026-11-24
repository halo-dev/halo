<script lang="ts" setup>
const itemWidths = [52, 68, 44, 60, 48];
</script>

<template>
  <div class="p-3">
    <ul v-for="y in 2" :key="y" class="mt-5 first:mt-0">
      <li
        v-for="x in 5"
        :key="x"
        class="flex items-center rounded-base py-[0.4rem]"
      >
        <div
          class="mr-3 size-[1.2rem] animate-pulse rounded-lg bg-gray-200"
        ></div>
        <div
          class="h-6 animate-pulse rounded-lg bg-gray-200"
          :style="{ width: `${itemWidths[x - 1]}%` }"
        ></div>
      </li>
    </ul>
  </div>
</template>
