<script setup lang="ts">
import { usePermission } from "@/utils/permission";
import ActionButton from "@console/modules/dashboard/components/ActionButton.vue";
import WidgetConfigFormModal from "@console/modules/dashboard/components/WidgetConfigFormModal.vue";
import { IconCloseCircle, IconSettings } from "@halo-dev/components";
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { computed, inject, ref, type ComputedRef } from "vue";
import type { SimpleWidget } from "../types";

const props = defineProps<{
  item: SimpleWidget;
}>();

const emit = defineEmits<{
  (e: "remove"): void;
  (e: "update:config", config: Record<string, unknown>): void;
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

const defaultSize = computed(() => {
  return widgetDefinition.value?.defaultSize || { w: 1, h: 1 };
});

const configModalVisible = ref(false);

function handleSaveConfig(config: Record<string, unknown>) {
  emit("update:config", config);
  configModalVisible.value = false;
}
</script>
<template>
  <div
    v-if="currentUserHasPermission(widgetDefinition?.permissions)"
    class="group/grid-item relative"
    :style="{
      width: `${defaultSize.w * 100}px`,
      height: `${defaultSize.h * 36}px`,
    }"
  >
    <component
      :is="widgetDefinition?.component"
      edit-mode
      :config="item.config"
      @update:config="handleSaveConfig"
    />
    <div
      class="absolute right-0 top-0 hidden h-8 items-center overflow-hidden rounded-tr-lg bg-gray-100 group-hover/grid-item:inline-flex"
    >
      <slot name="actions" />
      <ActionButton
        v-if="widgetDefinition?.configFormKitSchema"
        class="bg-black"
        @click="configModalVisible = true"
      >
        <IconSettings />
      </ActionButton>
      <ActionButton class="bg-red-500" @click="emit('remove')">
        <IconCloseCircle />
      </ActionButton>
    </div>
  </div>
  <WidgetConfigFormModal
    v-if="
      widgetDefinition &&
      widgetDefinition.configFormKitSchema &&
      configModalVisible
    "
    :widget-definition="widgetDefinition"
    :config="item.config"
    @close="configModalVisible = false"
    @save="handleSaveConfig"
  />
</template>
