<script lang="ts" setup>
import { VEntityField, VStatusDot } from "@halo-dev/components";

withDefaults(
  defineProps<{
    tooltip?: string;
    state?: "default" | "success" | "warning" | "error";
    animate?: boolean;
    text?: string;
  }>(),
  {
    tooltip: undefined,
    state: "default",
    animate: false,
    text: undefined,
  }
);
</script>

<template>
  <VEntityField>
    <template #description>
      <VStatusDot
        v-tooltip="tooltip"
        :state="state"
        :animate="animate"
        :text="text"
      />
    </template>
  </VEntityField>
</template>
