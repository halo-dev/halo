<script lang="ts" setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);

const handleChange = () => {
  emit("update:modelValue", !props.modelValue);
  emit("change", !props.modelValue);
};
</script>
<template>
  <div class="switch-wrapper">
    <button
      :class="{
        'bg-gray-200': !modelValue,
        '!bg-primary': modelValue,
      }"
      aria-checked="false"
      class="switch-inner"
      role="switch"
      type="button"
      @click="handleChange"
    >
      <span
        :class="{
          'translate-x-0': !modelValue,
          'translate-x-5': modelValue,
        }"
        aria-hidden="true"
        class="switch-indicator"
      >
        <slot name="icon" />
      </span>
    </button>
  </div>
</template>
<style lang="scss">
.switch-wrapper {
  @apply inline-flex;
  @apply box-border;

  .switch-inner {
    @apply relative;
    @apply inline-flex;
    @apply flex-shrink-0;
    @apply h-6;
    @apply w-11;
    @apply border-2;
    @apply border-transparent;
    @apply rounded-full;
    @apply cursor-pointer;
    @apply transition-colors;
    @apply ease-in-out;
    @apply duration-200;

    .switch-indicator {
      @apply pointer-events-none;
      @apply inline-block;
      @apply h-5;
      @apply w-5;
      @apply rounded-full;
      @apply bg-white;
      @apply shadow;
      @apply transform;
      @apply ring-0;
      @apply transition;
      @apply ease-in-out;
      @apply duration-200;
    }
  }
}
</style>
