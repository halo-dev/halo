<script lang="ts" setup>
import { VCodemirror } from "./index";

function initState() {
  return {
    value: "Hello Halo",
  };
}
</script>
<template>
  <Story :initState="initState" title="Codemirror">
    <template #default="{ state }">
      <VCodemirror v-model="state.value" height="500px" language="yaml" />
    </template>
  </Story>
</template>
