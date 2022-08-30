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
  return [`input-${props.size}`];
});

function handleInput(e: Event) {
  const { value } = e.target as HTMLInputElement;
  emit("update:modelValue", value);
}
</script>
<template>
  <div class="input-wrapper">
    <div v-if="$slots.prefix" class="input-prefix">
      <slot name="prefix" />
    </div>
    <input
      :class="classes"
      :disabled="disabled"
      :placeholder="placeholder"
      :value="modelValue"
      type="text"
      @input="handleInput"
    />
    <div v-if="$slots.suffix" class="input-suffix">
      <slot name="suffix" />
    </div>
  </div>
</template>

<style lang="scss">
.input-wrapper {
  @apply box-border
  relative
  w-full
  inline-flex;
  input {
    @apply outline-0
    bg-white
    antialiased
    resize-none
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

    &.input-lg {
      @apply h-11
      px-4
      text-lg;
    }

    &.input-md {
      @apply h-9
      px-3
      text-sm;
    }

    &.input-sm {
      @apply h-7
      px-3
      text-xs;
    }

    &.input-xs {
      @apply h-6
      px-2
      text-xs;
    }
  }

  .input-prefix {
    position: absolute;
    display: flex;
    top: 50%;
    transform: translateY(-50%);
    align-items: center;
    justify-content: center;
  }

  .input-suffix {
    position: absolute;
    display: flex;
    top: 50%;
    right: 0;
    transform: translateY(-50%);
    align-items: center;
    justify-content: center;
  }
}
</style>
