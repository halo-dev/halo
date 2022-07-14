<script lang="ts" setup>
defineProps({
  modelValue: {
    type: String,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  placeholder: {
    type: String,
  },
  rows: {
    type: Number,
    default: 3,
  },
});

const emit = defineEmits(["update:modelValue"]);

function handleInput(e: Event) {
  const { value } = e.target as HTMLInputElement;
  emit("update:modelValue", value);
}
</script>
<template>
  <div class="textarea-wrapper">
    <textarea
      :disabled="disabled"
      :placeholder="placeholder"
      :rows="rows"
      :value="modelValue"
      @input="handleInput"
    >
    </textarea>
  </div>
</template>
<style lang="scss">
.textarea-wrapper {
  @apply box-border;
  @apply relative;
  @apply w-full;
  @apply inline-flex;
  textarea {
    @apply outline-0;
    @apply bg-white;
    @apply antialiased;
    @apply w-full;
    @apply text-black;
    @apply block;
    @apply transition-all;
    @apply appearance-none;

    @apply p-3;
    @apply text-sm;
    @apply rounded-base;
    border: 1px solid #ced4da;

    &:active {
      @apply border-primary;
    }

    &:focus {
      @apply border-primary;
    }

    &:disabled {
      @apply opacity-50;
      @apply cursor-not-allowed;
    }
  }
}
</style>
