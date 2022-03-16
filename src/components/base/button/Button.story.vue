<template>
  <Story title="Button">
    <Variant :init-state="initState" title="playground">
      <template #default="{ state }">
        <VButton :type="state.type"> Hello world </VButton>
      </template>
    </Variant>

    <Variant icon="carbon:star-filled" icon-color="#10B981" title="secondary">
      <VButton type="secondary"> Hello world </VButton>
    </Variant>
  </Story>
</template>
<script setup>
import { VButton } from "./index";

function initState() {
  return {
    type: "primary",
  };
}
</script>
