<script lang="ts" setup>
import { IconArrowUpLine } from "@halo-dev/components";
import { useEventListener } from "@vueuse/core";
import { ref } from "vue";

const visible = ref(false);

function onWindowScroll() {
  const isDown = window.scrollY > 300;
  visible.value = isDown;
}

function handleScrollToTop() {
  window.scrollTo({ top: 0, behavior: "smooth" });
}

useEventListener(window, "scroll", onWindowScroll);
</script>
<template>
  <button
    class="fixed bottom-8 right-8 z-10 inline-flex size-10 items-center justify-center rounded-full bg-primary transition-all hover:bg-primary/80 hover:shadow active:scale-95 active:bg-primary"
    :class="{
      'opacity-100': visible,
      'opacity-0': !visible,
    }"
    @click="handleScrollToTop"
  >
    <IconArrowUpLine class="size-6 text-white" />
  </button>
</template>
