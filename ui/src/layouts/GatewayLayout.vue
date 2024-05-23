<script lang="ts" setup>
import IconLogo from "~icons/core/logo?width=5rem&height=2rem";
</script>

<template>
  <div
    class="flex h-screen flex-col items-center overflow-auto bg-white/90 pt-[30vh]"
  >
    <IconLogo class="mb-8 flex-none" />

    <RouterView />
  </div>
</template>
