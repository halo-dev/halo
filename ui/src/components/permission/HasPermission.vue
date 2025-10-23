<script lang="ts" setup>
import { utils } from "@halo-dev/console-shared";

withDefaults(
  defineProps<{
    permissions: string[];
    any?: boolean;
  }>(),
  {
    any: true,
  }
);
</script>

<template>
  <slot v-if="utils.permission.has(permissions, any)" />
</template>
