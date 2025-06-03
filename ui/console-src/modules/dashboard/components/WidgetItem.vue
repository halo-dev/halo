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
    :h="item.h"
    :i="item.i"
    :w="item.w"
    :x="item.x"
    :y="item.y"
  >
    <component :is="item.componentName" :config="item.config" />
    <div
      class="absolute p-2 right-0 top-0 rounded-base bg-gray-100 inline-flex gap-2"
    >
      <IconSettings
        v-if="widgetDefinition?.configFormKitSchema?.length"
        class="cursor-pointer text-lg text-gray-500 hover:text-gray-900"
        @click="configModalVisible = true"
      />
      <IconCloseCircle
        class="cursor-pointer text-lg text-gray-500 hover:text-gray-900"
        @click="emit('remove')"
      />
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
