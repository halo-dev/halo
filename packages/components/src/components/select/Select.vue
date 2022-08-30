<script lang="ts" setup>
import { computed } from "vue";
import type { Size } from "./interface";

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    size?: Size;
    disabled?: boolean;
    placeholder?: string;
  }>(),
  {
    modelValue: undefined,
    size: "md",
    disabled: false,
    placeholder: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();

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
  @apply box-border
  relative
  w-full
  inline-flex;

  select {
    @apply outline-0
    bg-white
    antialiased
    w-full
    text-black
    block
    transition-all
    appearance-none
    rounded-base;
    border: 1px solid #ced4da;

    &:active {
      @apply border-primary;
    }

    &:focus {
      @apply border-primary;
    }

    &:disabled {
      @apply opacity-50
      cursor-not-allowed;
    }

    &.select-lg {
      @apply h-11
      px-4
      text-lg;
    }

    &.select-md {
      @apply h-9
      px-3
      text-sm;
    }

    &.select-sm {
      @apply h-7
      px-3
      text-xs;
    }

    &.select-xs {
      @apply h-6
      px-2
      text-xs;
    }
  }
}
</style>
