<template>
  <button
    :class="classes"
    :disabled="disabled"
    class="btn"
    @click="handleClick"
  >
    <span v-if="$slots.icon || loading" class="btn-icon">
      <svg
        v-if="loading"
        class="animate-spin"
        fill="none"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        ></circle>
        <path
          class="opacity-75"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          fill="currentColor"
        ></path>
      </svg>
      <slot v-else name="icon" />
    </span>
    <span class="btn-content">
      <slot />
    </span>
  </button>
</template>
<script lang="ts" setup>
import type { PropType } from "vue";
import { computed } from "vue";
import type { RouteLocationRaw } from "vue-router";
import { useRouter } from "vue-router";
import type { Size, Type } from "./interface";

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
  route: {
    type: Object as PropType<RouteLocationRaw>,
  },
});

const router = useRouter();
const emit = defineEmits(["click"]);

const classes = computed(() => {
  return [
    `btn-${props.size}`,
    `btn-${props.type}`,
    { "btn-circle": props.circle },
    { "btn-block": props.block },
    { "btn-loading": props.loading },
  ];
});

function handleClick() {
  if (props.disabled || props.loading) return;
  if (props.route) {
    router.push(props.route);
  }
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
  @apply border-none;
  @apply appearance-none;
  @apply align-middle;

  &:hover {
    @apply opacity-90;
  }

  &:active {
    @apply opacity-100;
  }

  &:disabled {
    @apply opacity-50;
    @apply cursor-not-allowed;
  }
}

.btn-default {
  border: 1px solid #d9d9d9;

  .btn-icon {
    color: #0e1731;
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
}

.btn-icon {
  @apply h-5 w-5;
  @apply text-white;
  @apply mr-3;
}

.btn-loading {
  @apply cursor-not-allowed;
  &:hover {
    @apply opacity-100;
  }
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

  .btn-icon {
    @apply h-3 w-3;
    @apply mr-2;
  }
}

.btn-xs {
  @apply h-6;
  @apply px-2;
  @apply text-xs;

  .btn-icon {
    @apply h-3 w-3;
    @apply mr-2;
  }
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
