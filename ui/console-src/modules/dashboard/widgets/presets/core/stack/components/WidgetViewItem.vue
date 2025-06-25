<script setup lang="ts">
import { usePermission } from "@/utils/permission";
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { computed, inject, type ComputedRef } from "vue";
import type { SimpleWidget } from "../types";

const props = defineProps<{
  item: SimpleWidget;
}>();

const { currentUserHasPermission } = usePermission();

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
    v-if="currentUserHasPermission(widgetDefinition?.permissions)"
    class="relative h-full w-full"
  >
    <component :is="widgetDefinition?.component" :config="item.config" />
  </div>
</template>
