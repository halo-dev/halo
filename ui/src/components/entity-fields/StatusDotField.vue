<script lang="ts" setup>
import {
  VEntityField,
  VStatusDot,
  type StatusDotState,
} from "@halo-dev/components";

withDefaults(
  defineProps<{
    /**
     * @deprecated Use `label`, which the status dot renders as both the
     * accessible name and the tooltip. Kept for existing extension points.
     */
    tooltip?: string;
    state?: StatusDotState;
    animate?: boolean;
    text?: string;
    label?: string;
  }>(),
  {
    tooltip: undefined,
    state: "default",
    animate: false,
    text: undefined,
    label: undefined,
  }
);
</script>

<template>
  <VEntityField>
    <template #description>
      <VStatusDot
        :state="state"
        :animate="animate"
        :text="text"
        :label="label ?? tooltip"
      />
    </template>
  </VEntityField>
</template>
