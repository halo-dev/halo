<script lang="ts" setup>
import { VCheckbox } from "./index";

const props = withDefaults(
  defineProps<{
    modelValue?: string[];
    options?: Array<Record<string, string>>;
    valueKey?: string;
    labelKey?: string;
    name?: string;
  }>(),
  {
    modelValue: () => [],
    valueKey: "value",
    labelKey: "label",
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string[]): void;
  (event: "change", value: string[]): void;
}>();

function handleChange(e: Event) {
  const { value, checked } = e.target as HTMLInputElement;
  const checkedValues = [...props.modelValue];

  if (checked) {
    checkedValues.push(value);
  } else {
    checkedValues.splice(checkedValues.indexOf(value), 1);
  }
  emit("update:modelValue", checkedValues);
  emit("change", checkedValues);
}
</script>
<template>
  <div class="checkbox-group-wrapper">
    <VCheckbox
      v-for="(option, index) in options"
      :key="index"
      :checked="modelValue.includes(option[valueKey])"
      :label="option[labelKey]"
      :name="name"
      :value="option[valueKey]"
      @change="handleChange"
    />
  </div>
</template>
<style lang="scss">
.checkbox-group-wrapper {
}
</style>
