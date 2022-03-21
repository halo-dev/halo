<template>
  <button
    class="btn"
    :class="classes"
    :disabled="disabled"
    @click="handleClick"
  >
    <slot />
  </button>
</template>
<script lang="ts" setup>
import type { PropType } from "vue";
import type { Size, Type } from "@/components/base/button/interface";
import { computed } from "vue";

const props = defineProps({
  type: {
    type: String as PropType<Type>,
    default: "default",
  },
  size: {
    type: String as PropType<Size>,
    default: "md",
  },
  circle: {
    type: Boolean,
    default: false,
  },
  block: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["click"]);

const classes = computed(() => {
  return [
    `btn-${props.size}`,
    `btn-${props.type}`,
    { "btn-circle": props.circle },
    { "btn-block": props.block },
  ];
});

function handleClick() {
  if (props.disabled || props.loading) return;
  emit("click");
}
</script>
<style lang="scss">
.btn {
  border-radius: 4px;
  @apply inline-flex;
  @apply flex-shrink-0;
  @apply cursor-pointer;
  @apply select-none;
  @apply flex-wrap;
  @apply items-center;
  @apply justify-center;
  @apply transition-all;
  @apply text-center;
  @apply text-sm;
  @apply no-underline;
  @apply h-9;
  @apply px-4;
  @apply outline-0;

  &:hover {
    @apply opacity-90;
  }

  &:active {
    @apply opacity-100;
  }

  &:disabled {
    @apply opacity-50;
  }
}

.btn-primary {
  background: #4ccba0;
  @apply text-white;
}

.btn-secondary {
  background: #0e1731;
  @apply text-white;
}

.btn-danger {
  background: #d71d1d;
  @apply text-white;
}

.btn-block {
  @apply w-full;
  @apply block;
}

.btn-lg {
  @apply h-11;
  @apply px-5;
  @apply text-lg;
}

.btn-sm {
  @apply h-7;
  @apply px-3;
  @apply text-xs;
}

.btn-xs {
  @apply h-6;
  @apply px-2;
  @apply text-xs;
}

.btn-circle {
  @apply w-9;
  @apply p-0;
  @apply rounded-full;
}

.btn-lg.btn-circle {
  @apply w-11;
}

.btn-sm.btn-circle {
  @apply w-7;
}

.btn-xs.btn-circle {
  @apply w-6;
}
</style>
