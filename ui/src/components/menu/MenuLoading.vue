<script lang="ts" setup></script>

<template>
  <div class="p-3">
    <ul v-for="y in 2" :key="y" class="mt-5 first:mt-0">
      <li
        v-for="x in 5"
        :key="x"
        class="flex h-[36.8px] items-center rounded-base py-[0.4rem]"
      >
        <div class="mr-3 size-5 animate-pulse rounded-lg bg-gray-200"></div>
        <div
          class="h-5 animate-pulse rounded-lg bg-gray-200"
          :style="{
            width: `${Math.random() * 50 + 10}%`,
          }"
        ></div>
      </li>
    </ul>
  </div>
</template>
