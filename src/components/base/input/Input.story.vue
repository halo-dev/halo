<template>
  <Story :init-state="initState" title="Input">
    <template #default="{ state }">
      <div class="w-1/2">
        <VInput
          v-model="state.value"
          :disabled="state.disabled"
          :placeholder="state.placeholder"
          :size="state.size"
        />
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
import { VInput } from "@/components/base/input/index";

function initState() {
  return {
    value: "Hello Halo",
    disabled: false,
    placeholder: "",
    size: "md",
  };
}
</script>
