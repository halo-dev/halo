<script lang="ts" setup>
import { VRadio } from "./index";
import type { PropType } from "vue";

defineProps({
  modelValue: {
    type: [String, Number, Boolean],
  },
  options: {
    type: Object as PropType<Array<Record<string, string | number | boolean>>>,
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
