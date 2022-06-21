<script lang="ts" setup>
import { VSwitch } from "./index";

function initState() {
  return {
    value: false,
  };
}
</script>
<template>
  <Story :init-state="initState" title="Switch">
    <template #default="{ state }">
      <div class="p-4">
        <VSwitch v-model="state.value"></VSwitch>
      </div>
    </template>
  </Story>
</template>
