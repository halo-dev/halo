<script lang="ts" setup>
import {
  IconDashboard,
  IconSettings,
  VButton,
  VPageHeader,
} from "@halo-dev/components";
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { computed, type ComputedRef, provide, ref, toRaw } from "vue";
import WidgetViewItem from "./components/WidgetViewItem.vue";
import { useDashboardExtensionPoint } from "./composables/use-dashboard-extension-point";
import { useDashboardWidgetsViewFetch } from "./composables/use-dashboard-widgets-fetch";
import "./styles/dashboard.css";
import { internalWidgetDefinitions } from "./widgets";

const currentBreakpoint = ref();

function breakpointChangedEvent(breakpoint: string) {
  currentBreakpoint.value = breakpoint;
}

const { data } = useDashboardWidgetsViewFetch(currentBreakpoint);

const { widgetDefinitions } = useDashboardExtensionPoint();

const availableWidgetDefinitions = computed(() => {
  return [...internalWidgetDefinitions, ...widgetDefinitions.value];
});

provide<ComputedRef<DashboardWidgetDefinition[]>>(
  "availableWidgetDefinitions",
  availableWidgetDefinitions
);
</script>
<template>
  <VPageHeader :title="$t('core.dashboard.title')">
    <template #icon>
      <IconDashboard />
    </template>
    <template #actions>
      <VButton
        type="secondary"
        @click="$router.push({ name: 'DashboardDesigner' })"
      >
        <template #icon>
          <IconSettings />
        </template>
        {{ $t("core.dashboard.actions.setting") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="dashboard m-4">
    <grid-layout
      :layout="toRaw(data?.layout || [])"
      :responsive-layouts="data?.layouts"
      :col-num="12"
      :is-draggable="false"
      :is-resizable="false"
      :margin="[10, 10]"
      :responsive="true"
      :row-height="30"
      :use-css-transforms="true"
      :vertical-compact="true"
      :breakpoints="{ lg: 1200, md: 996, sm: 768, xs: 480 }"
      :cols="{ lg: 12, md: 12, sm: 6, xs: 4 }"
      @breakpoint-changed="breakpointChangedEvent"
    >
      <WidgetViewItem v-for="item in data?.layout" :key="item.i" :item="item" />
    </grid-layout>
  </div>
</template>

<style scoped>
:deep(.vue-grid-item) {
  -ms-touch-action: unset !important;
  touch-action: unset !important;
}
</style>
