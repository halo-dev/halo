<script lang="ts" setup>
import { VSkeleton } from "@halo-dev/components";

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
        <VSkeleton class="mr-3 rounded-lg" width="1.2rem" height="1.2rem" />
        <VSkeleton
          class="rounded-lg"
          :width="`${itemWidths[x - 1]}%`"
          height="1.5rem"
        />
      </li>
    </ul>
  </div>
</template>
