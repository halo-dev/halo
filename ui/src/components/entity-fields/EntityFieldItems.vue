<script lang="ts" setup>
import { utils, type EntityFieldItem } from "@halo-dev/console-shared";

withDefaults(
  defineProps<{
    fields: EntityFieldItem[];
  }>(),
  {}
);
</script>

<template>
  <template
    v-for="(field, index) in fields"
    :key="`${field.position}-${index}`"
  >
    <component
      :is="field.component"
      v-bind="field.props"
      v-if="!field.hidden && utils.permission.has(field.permissions || [])"
    />
  </template>
</template>
