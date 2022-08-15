<script lang="ts" setup>
withDefaults(
  defineProps<{
    modelValue?: string;
    disabled?: boolean;
    placeholder?: string;
    rows?: number;
  }>(),
  {
    disabled: false,
    rows: 3,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();

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
  @apply box-border
  relative
  w-full
  inline-flex;
  textarea {
    @apply outline-0
    bg-white
    antialiased
    w-full
    text-black
    block
    transition-all
    appearance-none
    p-3
    text-sm
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
  }
}
</style>
