<script lang="ts" setup>
const props = withDefaults(
  defineProps<{
    modelValue?: boolean;
  }>(),
  {
    modelValue: false,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
  (event: "change", value: boolean): void;
}>();

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
  @apply inline-flex
  box-border;

  .switch-inner {
    @apply relative
    inline-flex
    flex-shrink-0
    h-6
    w-11
    border-2
    border-transparent
    rounded-full
    cursor-pointer
    transition-colors
    ease-in-out
    duration-200;

    .switch-indicator {
      @apply pointer-events-none
      inline-block
      h-5
      w-5
      rounded-full
      bg-white
      shadow
      transform
      ring-0
      transition
      ease-in-out
      duration-200;
    }
  }
}
</style>
