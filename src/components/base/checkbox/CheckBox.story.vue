<template>
  <Story :init-state="initState" title="CheckBox">
    <template #default="{ state }">
      <VCheckboxGroup
        v-model="state.value"
        :options="checkboxData"
        name="fruit"
      />
      Checked: {{ state.value.join(", ") }}
    </template>
  </Story>
</template>
<script lang="ts" setup>
import { VCheckboxGroup } from "@/components/base/checkbox";

function initState() {
  return {
    value: ["apple"],
  };
}

const checkboxData = [
  {
    value: "apple",
    label: "Apple",
  },
  {
    value: "banana",
    label: "Banana",
  },
  {
    value: "orange",
    label: "Orange",
  },
];
</script>
