<script lang="ts" setup>
import { VAvatar, VEntityField } from "@halo-dev/components";
import type { Plugin } from "@halo-dev/api-client";
withDefaults(
  defineProps<{
    plugin: Plugin;
  }>(),
  {}
);
</script>

<template>
  <VEntityField>
    <template #description>
      <VAvatar
        :alt="plugin.spec.displayName"
        :src="plugin.status?.logo"
        size="md"
      ></VAvatar>
    </template>
  </VEntityField>
</template>
