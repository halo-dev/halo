<script lang="ts" setup>
import { usePermission } from "@/utils/permission";
import type { EntityFieldItem } from "@halo-dev/console-shared";

withDefaults(
  defineProps<{
    fields: EntityFieldItem[];
  }>(),
  {}
);

const { currentUserHasPermission } = usePermission();
</script>

<template>
  <template
    v-for="(field, index) in fields"
    :key="`${field.position}-${index}`"
  >
    <component
      :is="field.component"
      v-bind="field.props"
      v-if="!field.hidden && currentUserHasPermission(field.permissions)"
    />
  </template>
</template>
