<script setup lang="ts">
import {
  utils,
  type DashboardWidget,
  type DashboardWidgetDefinition,
} from "@halo-dev/console-shared";
import { inject, type ComputedRef } from "vue";

defineProps<{
  item: DashboardWidget;
}>();

const availableWidgetDefinitions = inject<
  ComputedRef<DashboardWidgetDefinition[]>
>("availableWidgetDefinitions");

const getWidgetDefinition = (id: string) => {
  return availableWidgetDefinitions?.value?.find(
    (definition) => definition.id === id
  );
};
</script>

<template>
  <grid-item
    v-if="utils.permission.has(item.permissions || [])"
    :key="item.i"
    :h="item.h"
    :i="item.i"
    :w="item.w"
    :x="item.x"
    :y="item.y"
    :min-w="item.minW"
    :min-h="item.minH"
    :max-w="item.maxW"
    :max-h="item.maxH"
  >
    <component
      :is="getWidgetDefinition(item.id)?.component"
      :config="item.config"
    />
  </grid-item>
</template>
