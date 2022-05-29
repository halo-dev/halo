<script lang="ts" setup>
import type { PropType } from "vue";
import { computed } from "vue";
import type { Size } from "./interface";

const props = defineProps({
  modelValue: {
    type: String,
  },
  size: {
    type: String as PropType<Size>,
    default: "md",
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  placeholder: {
    type: String,
  },
});

const emit = defineEmits(["update:modelValue"]);

const classes = computed(() => {
  return [`select-${props.size}`];
});

function handleChange(e: Event) {
  const { value } = e.target as HTMLSelectElement;
  emit("update:modelValue", value);
}
</script>
<template>
  <div class="select-wrapper">
    <select
      :class="classes"
      :disabled="disabled"
      :value="modelValue"
      @change="handleChange"
    >
      <option v-if="placeholder" key="placeholder" disabled hidden value="">
        {{ placeholder }}
      </option>
      <slot />
    </select>
  </div>
</template>
<style lang="scss">
.select-wrapper {
  @apply box-border;
  @apply relative;
  @apply w-full;
  @apply inline-flex;

  select {
    @apply outline-0;
    @apply bg-white;
    @apply antialiased;
    @apply w-full;
    @apply text-black;
    @apply block;
    @apply transition-all;
    @apply appearance-none;
    border: 1px solid #ced4da;
    border-radius: 4px;

    &:active {
      border-color: #4ccba0;
    }

    &:focus {
      border-color: #4ccba0;
    }

    &:disabled {
      @apply opacity-50;
      @apply cursor-not-allowed;
    }

    &.select-lg {
      @apply h-11;
      @apply px-4;
      @apply text-lg;
    }

    &.select-md {
      @apply h-9;
      @apply px-3;
      @apply text-sm;
    }

    &.select-sm {
      @apply h-7;
      @apply px-3;
      @apply text-xs;
    }

    &.select-xs {
      @apply h-6;
      @apply px-2;
      @apply text-xs;
    }
  }
}
</style>
