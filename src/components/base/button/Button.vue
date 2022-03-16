<template>
  <button :class="[`btn-${size}`, `btn-${type}`]">
    <slot />
  </button>
</template>
<script lang="ts" setup>
import type { PropType } from "vue";
import type { Size, Type } from "@/components/base/button/interface";

defineProps({
  type: {
    type: String as PropType<Type>,
    default: "default",
  },
  size: {
    type: String as PropType<Size>,
    default: "md",
  },
  loading: {
    type: Boolean,
    default: false,
  },
});
</script>
<style>
.btn-secondary {
  @apply bg-black;
  @apply text-white;
}
</style>
