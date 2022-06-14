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
  @apply box-border;
  @apply relative;
  @apply w-full;
  @apply inline-flex;
  input {
    @apply outline-0;
    @apply bg-white;
    @apply antialiased;
    @apply resize-none;
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

    &.input-lg {
      @apply h-11;
      @apply px-4;
      @apply text-lg;
    }

    &.input-md {
      @apply h-9;
      @apply px-3;
      @apply text-sm;
    }

    &.input-sm {
      @apply h-7;
      @apply px-3;
      @apply text-xs;
    }

    &.input-xs {
      @apply h-6;
      @apply px-2;
      @apply text-xs;
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
