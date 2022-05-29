<template>
  <Story :init-state="initState" title="Radio">
    <template #default="{ state }">
      <VRadioGroup v-model="state.value" :options="radioData"></VRadioGroup>

      <VRadio
        v-for="(option, index) in radioData"
        :key="index"
        v-model="state.value"
        :label="option.label"
        :value="option.value"
        name="fruit"
      ></VRadio>
    </template>
  </Story>
</template>
<script lang="ts" setup>
import { VRadio, VRadioGroup } from "@/components/base/radio/index";

function initState() {
  return {
    value: "apple",
  };
}

const radioData = [
  {
    value: "banana",
    label: "Banana",
  },
  {
    value: "apple",
    label: "Apple",
  },
  {
    value: "orange",
    label: "Orange",
  },
];
</script>
