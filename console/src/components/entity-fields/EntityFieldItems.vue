<script lang="ts" setup>
import type { EntityFieldItem } from "packages/shared/dist";

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
      v-if="field.visible"
    />
  </template>
</template>
