<template>
  <button
    :class="classes"
    :disabled="disabled"
    class="btn"
    type="button"
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
import { computed } from "vue";
import type { RouteLocationRaw } from "vue-router";
import { useRouter } from "vue-router";
import type { Size, Type } from "./interface";

const props = withDefaults(
  defineProps<{
    type?: Type;
    size?: Size;
    circle?: boolean;
    block?: boolean;
    disabled?: boolean;
    loading?: boolean;
    route?: RouteLocationRaw | undefined;
  }>(),
  {
    type: "default",
    size: "md",
    circle: false,
    block: false,
    disabled: false,
    loading: false,
    route: undefined,
  }
);

const router = useRouter();
const emit = defineEmits<{
  (event: "click"): void;
}>();

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
  @apply rounded-base
  inline-flex
  flex-shrink-0
  cursor-pointer
  select-none
  flex-wrap
  items-center
  justify-center
  transition-all
  text-center
  text-sm
  no-underline
  h-9
  px-4
  outline-0
  border-none
  appearance-none
  align-middle;

  &:hover {
    @apply opacity-90;
  }

  &:active {
    @apply opacity-100;
  }

  &:disabled {
    @apply opacity-50
    cursor-not-allowed;
  }
}

.btn-default {
  border: 1px solid #d9d9d9;

  .btn-icon {
    @apply text-secondary;
  }
}

.btn-primary {
  @apply text-white bg-primary #{!important};
}

.btn-secondary {
  @apply text-white bg-secondary #{!important};
}

.btn-danger {
  background-color: #d71d1d !important;
  @apply text-white;
}

.btn-block {
  @apply w-full;
}

.btn-icon {
  @apply h-5 w-5
  text-white
  mr-3;
}

.btn-loading {
  @apply cursor-not-allowed;
  &:hover {
    @apply opacity-100;
  }
}

.btn-lg {
  @apply h-11
  px-5
  text-lg;
}

.btn-sm {
  @apply h-7
  px-3
  text-xs;

  .btn-icon {
    @apply h-3
    w-3
    mr-2;
  }
}

.btn-xs {
  @apply h-6
  px-2
  text-xs;

  .btn-icon {
    @apply h-3
    w-3
    mr-2;
  }
}

.btn-circle {
  @apply w-9
  p-0
  rounded-full;
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
