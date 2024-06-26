<script setup lang="ts">
import { useEventListener } from "@vueuse/core";
import { onMounted, ref } from "vue";

const props = withDefaults(
  defineProps<{
    position?: "top" | "bottom";
  }>(),
  {
    position: "top",
  }
);

const stickyBlock = ref<HTMLElement | null>(null);
const isSticky = ref(false);

function computeSticky() {
  if (!stickyBlock.value) return;
  const rect = stickyBlock.value?.getBoundingClientRect();

  if (props.position === "top") {
    isSticky.value = rect.top <= 0;
  } else {
    isSticky.value = rect.bottom >= window.innerHeight;
  }
}

onMounted(() => {
  computeSticky();
});

useEventListener("scroll", computeSticky);
</script>

<template>
  <div
    ref="stickyBlock"
    :class="{ 'sticky-element': true, 'sticky-shadow': isSticky }"
  >
    <slot />
  </div>
</template>

<style>
.sticky-element {
  position: sticky;
  bottom: 0;
}

.sticky-shadow {
  box-shadow: 0px -5px 10px -5px rgba(0, 0, 0, 0.1);
}
</style>
