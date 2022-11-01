<script lang="ts" setup>
import { VCodemirror } from "./index";

function initState() {
  return {
    value: "Hello Halo",
  };
}
</script>
<template>
  <Story :init-state="initState" title="Codemirror">
    <template #default="{ state }">
      <VCodemirror v-model="state.value" height="500px" language="html" />
    </template>
  </Story>
</template>
