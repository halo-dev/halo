<template>
  <Story :init-state="initState" title="Select">
    <template #default="{ state }">
      <div class="w-1/2">
        <VSelect
          v-model="state.value"
          :disabled="state.disabled"
          :placeholder="state.placeholder"
        >
          <VOption
            v-for="(option, index) in selectData"
            :key="index"
            :value="option.value"
          >
            {{ option.label }}
          </VOption>
        </VSelect>
      </div>
    </template>
    <template #controls="{ state }">
      <HstText v-model="state.value" title="Value" />
      <HstText v-model="state.placeholder" title="Placeholder" />
      <HstCheckbox v-model="state.disabled" title="Disabled" />
    </template>
  </Story>
</template>
<script lang="ts" setup>
import { VOption, VSelect } from "@/components/base/select/index";

function initState() {
  return {
    value: undefined,
    disabled: false,
    placeholder: "",
    size: "md",
  };
}

const selectData = [
  {
    value: "apple",
    label: "Apple",
  },
  {
    value: "banana",
    label: "Banana",
  },
];
</script>
