<script setup lang="ts">
import { usePermission } from "@/utils/permission";
import { VButton, VModal, VTabbar } from "@halo-dev/components";
import type { DashboardWidgetDefinition } from "packages/shared/dist";
import { computed, ref, useTemplateRef } from "vue";
import { useDashboardExtensionPoint } from "../composables/use-dashboard-extension-point";
import { internalWidgetDefinitions } from "../widgets";

const { currentUserHasPermission } = usePermission();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "add-widget", widget: DashboardWidgetDefinition): void;
}>();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const activeId = ref("");

const { widgetDefinitions } = useDashboardExtensionPoint();

const availableWidgetDefinitions = computed(() => {
  return [...internalWidgetDefinitions, ...widgetDefinitions.value];
});

const groupWidgetDefinitions = computed(() => {
  return availableWidgetDefinitions.value.reduce((acc, item) => {
    acc[item.group] = acc[item.group] || [];
    acc[item.group].push(item);
    return acc;
  }, {} as Record<string, DashboardWidgetDefinition[]>);
});

const groupWidgetDefinitionsKeys = computed(() => {
  return Object.keys(groupWidgetDefinitions.value);
});
</script>

<template>
  <VModal
    ref="modal"
    height="calc(100vh - 20px)"
    :width="1280"
    :layer-closable="true"
    :title="$t('core.dashboard.widgets.modal_title')"
    @close="emit('close')"
  >
    <VTabbar
      v-model:active-id="activeId"
      :items="[
        { id: '', label: 'All' },
        ...groupWidgetDefinitionsKeys.map((group) => {
          return { id: group, label: $t(group, group) };
        }),
      ]"
      type="outline"
    ></VTabbar>
    <div class="mt-4 flex flex-col gap-5">
      <template v-for="item in availableWidgetDefinitions" :key="item.name">
        <div
          v-if="
            activeId === '' ||
            (item.group === activeId &&
              currentUserHasPermission(item.permissions))
          "
          :style="{
            width: `${100 / (12 / item.defaultSize.w)}%`,
            height: `${item.defaultSize.h * 36}px`,
          }"
          class="cursor-pointer"
          @click="emit('add-widget', item)"
        >
          <div class="pointer-events-none w-full h-full">
            <component :is="item.componentName" />
          </div>
        </div>
      </template>
    </div>
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
