<script setup lang="ts">
import { usePermission } from "@/utils/permission";
import { IconCloseCircle, IconSettings } from "@halo-dev/components";
import type { DashboardWidget } from "@halo-dev/console-shared";
import { computed, ref } from "vue";
import { useDashboardExtensionPoint } from "../composables/use-dashboard-extension-point";
import { internalWidgetDefinitions } from "../widgets";
import WidgetConfigFormModal from "./WidgetConfigFormModal.vue";

const props = defineProps<{
  item: DashboardWidget;
}>();

const emit = defineEmits<{
  (e: "remove"): void;
  (e: "update", item: DashboardWidget): void;
}>();

const { currentUserHasPermission } = usePermission();

const { widgetDefinitions } = useDashboardExtensionPoint();

const availableWidgetDefinitions = computed(() => {
  return [...internalWidgetDefinitions, ...widgetDefinitions.value];
});

const widgetDefinition = computed(() => {
  return availableWidgetDefinitions.value.find(
    (definition) => definition.name === props.item.name
  );
});

const configModalVisible = ref(false);

function handleSaveConfig(config: Record<string, unknown>) {
  emit("update", {
    ...props.item,
    config,
  });
  configModalVisible.value = false;
}
</script>
<template>
  <grid-item
    v-if="currentUserHasPermission(item.permissions)"
    :key="item.i"
    class="group/grid-item"
    :h="item.h"
    :i="item.i"
    :w="item.w"
    :x="item.x"
    :y="item.y"
  >
    <component :is="item.componentName" :config="item.config" />
    <div
      class="absolute hidden h-8 right-0 top-0 rounded-tr-lg bg-gray-100 overflow-hidden group-hover/grid-item:inline-flex items-center"
    >
      <div
        v-if="widgetDefinition?.configFormKitSchema?.length"
        class="h-full w-8 flex cursor-pointer items-center justify-center bg-black hover:bg-gray-800 text-white"
      >
        <IconSettings class="text-base" @click="configModalVisible = true" />
      </div>
      <div
        class="h-full w-8 flex cursor-pointer items-center justify-center bg-red-500 hover:bg-red-600 text-white"
      >
        <IconCloseCircle class="text-base" @click="emit('remove')" />
      </div>
    </div>
  </grid-item>
  <WidgetConfigFormModal
    v-if="widgetDefinition && configModalVisible"
    :widget-definition="widgetDefinition"
    :config="item.config"
    @close="configModalVisible = false"
    @save="handleSaveConfig"
  />
</template>
