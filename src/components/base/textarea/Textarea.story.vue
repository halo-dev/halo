<template>
  <Story title="Textarea" :init-state="initState">
    <template #default="{ state }">
      <div class="w-1/2">
        <VTextarea
          v-model="state.value"
          :disabled="state.disabled"
          :placeholder="state.placeholder"
          :size="state.size"
          :rows="state.rows"
        />
      </div>
    </template>
    <template #controls="{ state }">
      <HstText v-model="state.value" title="Value" />
      <HstText v-model="state.placeholder" title="Placeholder" />
      <HstCheckbox v-model="state.disabled" title="Disabled" />
      <HstNumber v-model="state.rows" title="Rows"></HstNumber>
    </template>
  </Story>
</template>
<script lang="ts" setup>
import { VTextarea } from "@/components/base/textarea/index";

function initState() {
  return {
    value: "Hello Halo",
    disabled: false,
    placeholder: "",
    rows: 3,
  };
}
</script>
