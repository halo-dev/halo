<script lang="ts" setup>
import { randomUUID } from "@/utils/id";
import { usePermission } from "@/utils/permission";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  IconAddCircle,
  IconCloseCircle,
  IconComputer,
  IconDashboard,
  IconPhone,
  IconSave,
  IconSettings,
  IconTablet,
  VButton,
  VLoading,
  VSpace,
  VTabbar,
} from "@halo-dev/components";
import type {
  DashboardWidget,
  DashboardWidgetDefinition,
} from "@halo-dev/console-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { computed, defineComponent, h, markRaw, ref } from "vue";
import { useRouter } from "vue-router";
import WidgetsHubModal from "./components/WidgetsHubModal.vue";
import { useDashboardWidgetsFetch } from "./composables/use-dashboard-widgets-fetch";
import "./styles/dashboard.css";

const { currentUserHasPermission } = usePermission();
const queryClient = useQueryClient();
const router = useRouter();

const currentBreakpoint = ref("lg");

const deviceOptions = [
  { id: "lg", pixels: 1200, icon: markRaw(IconComputer) },
  {
    id: "md",
    pixels: 996,
    icon: markRaw(
      defineComponent({
        render() {
          return h(IconTablet, {
            class: "-rotate-90",
          });
        },
      })
    ),
  },
  {
    id: "sm",
    pixels: 768,
    icon: markRaw(IconTablet),
  },
  { id: "xs", pixels: 480, icon: markRaw(IconPhone) },
];

const currentDevice = computed(() => {
  return deviceOptions.find((option) => option.id === currentBreakpoint.value);
});

const designContainerStyles = computed(() => {
  if (currentBreakpoint.value === "lg") {
    return {};
  }
  return {
    width: `${currentDevice.value?.pixels}px`,
    margin: "1rem auto",
    border: "1px dashed #e0e0e0",
    borderRadius: "0.5rem",
    padding: "2px",
  };
});

function breakpointChangedEvent(breakpoint: string) {
  console.log(breakpoint);
  currentBreakpoint.value = breakpoint;
  if (!layout.value) {
    return;
  }
  layout.value = layouts.value[breakpoint] || layout.value["lg"] || [];
  console.log(layout.value);
}

const { layouts, layout, isLoading } =
  useDashboardWidgetsFetch(currentBreakpoint);

function handleAddWidget(widgetDefinition: DashboardWidgetDefinition) {
  const newWidget: DashboardWidget = {
    i: randomUUID(),
    x: 0,
    y: 0,
    w: widgetDefinition.defaultSize.w,
    h: widgetDefinition.defaultSize.h,
    name: widgetDefinition.name,
    componentName: widgetDefinition.componentName,
    config: widgetDefinition.defaultConfig,
    permissions: widgetDefinition.permissions,
  };

  const newLayout = [...layout.value, newWidget];

  layout.value = newLayout;
  layouts.value = {
    ...layouts.value,
    [currentBreakpoint.value]: newLayout,
  };

  widgetsHubModalVisible.value = false;
}

function handleRemove(item: DashboardWidget) {
  const cloneWidgets = cloneDeep(layout.value);
  cloneWidgets.splice(
    cloneWidgets.findIndex((widget) => widget.i === item.i),
    1
  );
  layout.value = cloneWidgets;
  layouts.value = {
    ...layouts.value,
    [currentBreakpoint.value]: cloneWidgets,
  };
}

const widgetsHubModalVisible = ref(false);

async function handleSave() {
  const { data } = await consoleApiClient.plugin.plugin.fetchPluginJsonConfig({
    name: "app-store-integration",
  });

  const configMapData = data as Record<string, any>;

  const dashboardData = {
    ...configMapData.dashboard,
    [currentBreakpoint.value]: layout.value,
  };

  if (currentBreakpoint.value === "xs") {
    dashboardData.xxs = layout.value;
  }

  await consoleApiClient.plugin.plugin.updatePluginJsonConfig({
    name: "app-store-integration",
    body: {
      ...data,
      dashboard: dashboardData,
    },
  });

  await queryClient.invalidateQueries({ queryKey: ["core:dashboard:widgets"] });

  router.replace({ name: "Dashboard" });
}
</script>
<template>
  <div
    class="flex items-center justify-between bg-white p-4 h-14 sticky top-0 z-10"
  >
    <div class="self-center">
      <h2 class="flex items-center truncate text-xl font-bold text-gray-800">
        <IconDashboard class="mr-2 self-center" />
        <span>{{ `编辑仪表盘` }}</span>
      </h2>
    </div>
    <div>
      <VTabbar
        v-model:active-id="currentBreakpoint"
        :items="deviceOptions as any"
        type="outline"
      ></VTabbar>
    </div>
    <div class="self-center">
      <VSpace>
        <VButton @click="widgetsHubModalVisible = true">
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          {{ $t("core.dashboard.actions.add_widget") }}
        </VButton>
        <VButton type="secondary" @click="handleSave">
          <template #icon>
            <IconSave class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.save") }}
        </VButton>
      </VSpace>
    </div>
  </div>

  <div class="dashboard m-4 transition-all" :style="designContainerStyles">
    <VLoading v-if="isLoading" />
    <grid-layout
      v-if="!isLoading"
      v-model:layout="layout"
      :responsive-layouts="layouts"
      :col-num="12"
      :is-draggable="true"
      :is-resizable="true"
      :margin="[10, 10]"
      :responsive="true"
      :row-height="30"
      :use-css-transforms="true"
      :vertical-compact="true"
      @breakpoint-changed="breakpointChangedEvent"
    >
      <template v-for="item in layout">
        <grid-item
          v-if="currentUserHasPermission(item.permissions)"
          :key="item.i"
          :h="item.h"
          :i="item.i"
          :w="item.w"
          :x="item.x"
          :y="item.y"
        >
          <component :is="item.componentName" />
          <div
            class="absolute p-2 right-0 top-0 rounded-base bg-gray-100 inline-flex gap-2"
          >
            <IconSettings
              class="cursor-pointer text-lg text-gray-500 hover:text-gray-900"
            />
            <IconCloseCircle
              class="cursor-pointer text-lg text-gray-500 hover:text-gray-900"
              @click="handleRemove(item)"
            />
          </div>
        </grid-item>
      </template>
    </grid-layout>
  </div>
  <WidgetsHubModal
    v-if="widgetsHubModalVisible"
    @close="widgetsHubModalVisible = false"
    @add-widget="handleAddWidget"
  />
</template>
