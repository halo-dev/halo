<script setup lang="ts">
import {
  utils,
  type DashboardWidgetDefinition,
} from "@halo-dev/console-shared";
import { computed, inject, type ComputedRef } from "vue";
import type { SimpleWidget } from "../types";

const props = defineProps<{
  item: SimpleWidget;
}>();

const availableWidgetDefinitions = inject<
  ComputedRef<DashboardWidgetDefinition[]>
>("availableWidgetDefinitions");

const widgetDefinition = computed(() => {
  return availableWidgetDefinitions?.value?.find(
    (definition) => definition.id === props.item.id
  );
});
</script>

<template>
  <div
    v-if="utils.permission.has(widgetDefinition?.permissions || [])"
    class="relative h-full w-full"
  >
    <component :is="widgetDefinition?.component" :config="item.config" />
  </div>
</template>
