<script lang="ts" setup>
import { VRadio } from "./index";

withDefaults(
  defineProps<{
    modelValue?: string | number | boolean;
    options?: Array<Record<string, string | number | boolean>>;
    valueKey?: string;
    labelKey?: string;
    name?: string;
  }>(),
  {
    modelValue: undefined,
    options: undefined,
    valueKey: "value",
    labelKey: "label",
    name: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string | number | boolean): void;
  (event: "change", value: string | number | boolean): void;
}>();

function handleChange(value: string | number | boolean) {
  emit("update:modelValue", value);
  emit("change", value);
}
</script>
<template>
  <div class="radio-group-wrapper">
    <VRadio
      v-for="(option, index) in options"
      :key="index"
      :label="option[labelKey] + ''"
      :model-value="modelValue"
      :name="name"
      :value="option[valueKey]"
      @change="handleChange"
    />
  </div>
</template>
<style lang="scss"></style>
