<template>
  <button :class="[`btn-${size}`, `btn-${type}`]" class="btn">
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
.btn {
  border-radius: 2px;
  @apply cursor-pointer;
  @apply border-none;
}

.btn-secondary {
  @apply bg-themeable-secondary;
  @apply text-white;
}

.btn-primary {
  @apply bg-themeable-primary;
  @apply text-black;
}
</style>
