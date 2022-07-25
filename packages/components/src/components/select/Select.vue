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
