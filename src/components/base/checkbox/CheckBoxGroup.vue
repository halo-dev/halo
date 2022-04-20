<script lang="ts" setup>
import { VCheckbox } from "./index";
import type { PropType } from "vue";

const props = defineProps({
  modelValue: {
    type: Object as PropType<Array<string>>,
    default: () => {
      return [];
    },
  },
  options: {
    type: Object as PropType<Array<Record<string, string>>>,
  },
  valueKey: {
    type: String,
    default: "value",
  },
  labelKey: {
    type: String,
    default: "label",
  },
  name: {
    type: String,
  },
});

const emit = defineEmits(["update:modelValue", "change"]);

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
