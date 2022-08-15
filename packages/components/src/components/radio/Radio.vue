<script lang="ts" setup>
import { computed } from "vue";

const props = defineProps<{
  modelValue?: string | number | boolean;
  value?: string | number | boolean;
  label?: string;
  name?: string;
}>();

const emit = defineEmits<{
  (event: "update:modelValue", value: string | number | boolean): void;
  (event: "change", value: string | number | boolean): void;
}>();

const id = ["radio", props.name, props.value]
  .filter((item) => !!item)
  .join("-");

const checked = computed(() => props.modelValue === props.value);

function handleChange(e: Event) {
  const { value } = e.target as HTMLInputElement;
  emit("update:modelValue", value);
  emit("change", value);
}
</script>
<template>
  <div :class="{ 'radio-wrapper-checked': checked }" class="radio-wrapper">
    <div class="radio-inner">
      <input
        :id="id"
        :checked="checked"
        :name="name"
        :value="value"
        type="radio"
        @change="handleChange"
      />
    </div>
    <label v-if="label" :for="id" class="radio-label">
      {{ label }}
    </label>
  </div>
</template>
<style lang="scss">
.radio-wrapper {
  @apply flex
  items-center
  box-border
  flex-grow-0;

  .radio-inner {
    @apply self-center
    relative;
  }

  .radio-label {
    @apply flex
    self-center
    items-start
    ml-3;
  }
}
</style>
