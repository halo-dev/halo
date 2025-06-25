<script setup lang="ts">
import { usePermission } from "@/utils/permission";
import { IconCloseCircle, IconSettings } from "@halo-dev/components";
import type {
  DashboardWidget,
  DashboardWidgetDefinition,
} from "@halo-dev/console-shared";
import { computed, inject, ref, type ComputedRef } from "vue";
import ActionButton from "./ActionButton.vue";
import WidgetConfigFormModal from "./WidgetConfigFormModal.vue";

const props = defineProps<{
  item: DashboardWidget;
}>();

const emit = defineEmits<{
  (e: "remove"): void;
  (e: "update", item: DashboardWidget): void;
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
    :min-w="item.minW"
    :min-h="item.minH"
    :max-w="item.maxW"
    :max-h="item.maxH"
  >
    <component
      :is="widgetDefinition?.component"
      edit-mode
      :config="item.config"
      @update:config="handleSaveConfig"
    />
    <div
      class="absolute right-0 top-0 z-[100] hidden h-8 items-center overflow-hidden rounded-tr-lg bg-gray-100 group-hover/grid-item:inline-flex"
    >
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
  </grid-item>
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
