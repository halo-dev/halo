<script lang="ts" setup>
import { IconDeleteBin } from "@halo-dev/components";
</script>

<template>
  <div
    class="group inline-flex cursor-pointer items-center justify-center rounded-full bg-primary px-1 py-1 opacity-80 ring-1 ring-primary transition-all duration-300 hover:opacity-100 hover:shadow-sm hover:ring-opacity-50"
  >
    <div class="h-4 w-4 rounded-full transition-all">
      <IconDeleteBin
        class="h-full w-full text-gray-200 transition-all group-hover:text-white"
      />
    </div>
  </div>
</template>
