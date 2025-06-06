<script lang="ts" setup>
import {
  IconDashboard,
  IconSettings,
  VButton,
  VPageHeader,
} from "@halo-dev/components";
import type { DashboardWidgetDefinition } from "packages/shared/dist";
import { computed, type ComputedRef, provide, ref } from "vue";
import WidgetViewItem from "./components/WidgetViewItem.vue";
import { useDashboardExtensionPoint } from "./composables/use-dashboard-extension-point";
import { useDashboardWidgetsFetch } from "./composables/use-dashboard-widgets-fetch";
import "./styles/dashboard.css";
import { internalWidgetDefinitions } from "./widgets";

const currentBreakpoint = ref();

function breakpointChangedEvent(breakpoint: string) {
  currentBreakpoint.value = breakpoint;
}

const { layouts, layout } = useDashboardWidgetsFetch(currentBreakpoint);

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
      <IconDashboard class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton
        type="secondary"
        @click="$router.push({ name: 'DashboardDesigner' })"
      >
        <template #icon>
          <IconSettings class="h-full w-full" />
        </template>
        {{ $t("core.dashboard.actions.setting") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="dashboard m-4">
    <grid-layout
      v-model:layout="layout"
      :responsive-layouts="layouts"
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
      <WidgetViewItem v-for="item in layout" :key="item.i" :item="item" />
    </grid-layout>
  </div>
</template>
