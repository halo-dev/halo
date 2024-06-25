<script lang="ts" setup>
import type { ExtensionDefinition } from "@halo-dev/api-client";
import { VEntity, VEntityField } from "@halo-dev/components";
withDefaults(defineProps<{ extensionDefinition: ExtensionDefinition }>(), {});
</script>

<template>
  <VEntity>
    <template v-if="$slots['selection-indicator']" #checkbox>
      <slot name="selection-indicator" />
    </template>
    <template #start>
      <VEntityField
        :title="extensionDefinition.spec.displayName"
        :description="extensionDefinition.spec.description"
      ></VEntityField>
    </template>
  </VEntity>
</template>
