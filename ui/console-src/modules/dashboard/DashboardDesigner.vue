<script lang="ts" setup>
import { randomUUID } from "@/utils/id";
import { ucApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconComputer,
  IconDashboard,
  IconPhone,
  IconSave,
  IconTablet,
  Toast,
  VButton,
  VDropdown,
  VDropdownItem,
  VTabbar,
} from "@halo-dev/components";
import type {
  DashboardWidget,
  DashboardWidgetDefinition,
} from "@halo-dev/console-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { useEventListener } from "@vueuse/core";
import { cloneDeep, isEqual } from "lodash-es";
import {
  computed,
  defineComponent,
  h,
  markRaw,
  provide,
  ref,
  useTemplateRef,
  watch,
  type ComputedRef,
} from "vue";
import type { GridLayout } from "vue-grid-layout";
import { useI18n } from "vue-i18n";
import { onBeforeRouteLeave, useRouter } from "vue-router";
import RiBox3Line from "~icons/ri/box-3-line";
import RiFileCopyLine from "~icons/ri/file-copy-line";
import WidgetEditableItem from "./components/WidgetEditableItem.vue";
import WidgetHubModal from "./components/WidgetHubModal.vue";
import { useDashboardExtensionPoint } from "./composables/use-dashboard-extension-point";
import { useDashboardWidgetsFetch } from "./composables/use-dashboard-widgets-fetch";
import "./styles/dashboard.css";
import { internalWidgetDefinitions } from "./widgets";

const { t } = useI18n();
const queryClient = useQueryClient();
const router = useRouter();

const currentBreakpoint = ref("lg");
const originalBreakpoint = ref();

const gridLayoutRef =
  useTemplateRef<InstanceType<typeof GridLayout>>("gridLayoutRef");

const { widgetDefinitions } = useDashboardExtensionPoint();

const availableWidgetDefinitions = computed(() => {
  return [...internalWidgetDefinitions, ...widgetDefinitions.value];
});

provide<ComputedRef<DashboardWidgetDefinition[]>>(
  "availableWidgetDefinitions",
  availableWidgetDefinitions
);

const { layouts, layout, originalLayout, isLoading } =
  useDashboardWidgetsFetch(currentBreakpoint);

const hasLayoutChanged = computed(() => {
  if (isLoading.value) {
    return false;
  }
  return !isEqual(originalLayout.value, layout.value);
});

watch(
  () => layout.value,
  () => {
    layouts.value[currentBreakpoint.value] = layout.value;
    if (currentBreakpoint.value === "xs") {
      layouts.value.xxs = layout.value;
    }
    if (gridLayoutRef.value) {
      gridLayoutRef.value.initResponsiveFeatures();
    }
  },
  {
    immediate: true,
    deep: true,
  }
);

const selectBreakpoint = ref();

async function handleBreakpointChange(breakpoint: string | number) {
  if (isLoading.value) {
    return;
  }

  if (hasLayoutChanged.value) {
    Toast.error(
      t("core.dashboard_designer.operations.change_breakpoint.tips_not_saved")
    );
    return;
  }

  selectBreakpoint.value = breakpoint;
}

function onBreakpointChange(breakpoint: string) {
  if (!originalBreakpoint.value) {
    originalBreakpoint.value = breakpoint;
  }
  if (!selectBreakpoint.value) {
    selectBreakpoint.value = breakpoint;
  }
  currentBreakpoint.value = breakpoint;
}

const deviceOptionDefinitions = [
  {
    id: "lg",
    pixels: 1200,
    text: t("core.dashboard_designer.breakpoints.lg"),
    icon: markRaw(IconComputer),
  },
  {
    id: "md",
    pixels: 996,
    text: t("core.dashboard_designer.breakpoints.md"),
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
    text: t("core.dashboard_designer.breakpoints.sm"),
    icon: markRaw(IconTablet),
  },
  {
    id: "xs",
    pixels: 480,
    text: t("core.dashboard_designer.breakpoints.xs"),
    icon: markRaw(IconPhone),
  },
];

const deviceOptions = computed(() => {
  const breakpointOrder = ["lg", "md", "sm", "xs"];
  const currentIndex = breakpointOrder.indexOf(originalBreakpoint.value);

  return deviceOptionDefinitions.filter((option) => {
    const optionIndex = breakpointOrder.indexOf(option.id);
    return optionIndex >= currentIndex;
  });
});

const designContainerStyles = computed(() => {
  if (currentBreakpoint.value === "lg" && !selectBreakpoint.value) {
    return {};
  }
  if (originalBreakpoint.value === "xs") {
    return {};
  }
  if (selectBreakpoint.value === "lg") {
    return {};
  }
  const device = deviceOptions.value.find(
    (option) => option.id === selectBreakpoint.value
  );
  return {
    width: `${device?.pixels}px`,
    margin: "1rem auto",
    border: "1px dashed #e0e0e0",
    borderRadius: "0.75rem",
    padding: "2px",
  };
});

function handleAddWidget(widgetDefinition: DashboardWidgetDefinition) {
  const zeroXWidgets = layout.value.filter((widget) => widget.x === 0);
  const maxY = zeroXWidgets.reduce((max, widget) => {
    return Math.max(max, widget.y + widget.h);
  }, 0);

  const newWidget: DashboardWidget = {
    i: randomUUID(),
    x: 0,
    y: maxY + 1,
    w: widgetDefinition.defaultSize.w,
    h: widgetDefinition.defaultSize.h,
    minW: widgetDefinition.defaultSize.minW,
    minH: widgetDefinition.defaultSize.minH,
    maxW: widgetDefinition.defaultSize.maxW,
    maxH: widgetDefinition.defaultSize.maxH,
    id: widgetDefinition.id,
    config: widgetDefinition.defaultConfig,
    permissions: widgetDefinition.permissions,
  };

  const newLayout = [...layout.value, newWidget];

  layout.value = newLayout;

  widgetsHubModalVisible.value = false;

  window.scrollTo({
    top: document.body.scrollHeight,
    behavior: "smooth",
  });
}

function handleRemove(item: DashboardWidget) {
  const widgetsToUpdate = cloneDeep(layout.value);
  widgetsToUpdate.splice(
    widgetsToUpdate.findIndex((widget) => widget.i === item.i),
    1
  );
  layout.value = widgetsToUpdate;
}

function handleUpdate(item: DashboardWidget) {
  const widgetsToUpdate = cloneDeep(layout.value);
  const index = widgetsToUpdate.findIndex((widget) => widget.i === item.i);
  widgetsToUpdate[index] = item;
  layout.value = widgetsToUpdate;
}

const widgetsHubModalVisible = ref(false);

const isSubmitting = ref(false);

async function handleSave() {
  try {
    isSubmitting.value = true;

    await ucApiClient.user.preference.updateMyPreference({
      group: "dashboard-widgets",
      body: layouts.value,
    });

    await queryClient.invalidateQueries({
      queryKey: ["core:dashboard:widgets"],
    });

    await queryClient.invalidateQueries({
      queryKey: ["core:dashboard:widgets:view"],
    });

    Toast.success(t("core.common.toast.save_success"));
  } catch (error) {
    console.error("Failed to save dashboard widgets config", error);
  } finally {
    isSubmitting.value = false;
  }
}

function handleBack() {
  router.replace({ name: "Dashboard" });
}

onBeforeRouteLeave((_, __, next) => {
  if (hasLayoutChanged.value) {
    handleShowLeaveWarning(next);
    return;
  }
  next();
});

function handleShowLeaveWarning(next: () => void) {
  Dialog.warning({
    title: t("core.dashboard_designer.operations.back.title"),
    description: t("core.dashboard_designer.operations.back.description"),
    confirmText: t("core.dashboard_designer.operations.back.confirm_text"),
    confirmType: "danger",
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: () => {
      next();
    },
  });
}

useEventListener(window, "beforeunload", (e) => {
  if (hasLayoutChanged.value) {
    e.preventDefault();
    e.returnValue = t("core.dashboard_designer.operations.back.description");
    return t("core.dashboard_designer.operations.back.description");
  }
});

function handleCopyFromLayout(breakpoint: string) {
  const layoutToCopy = layouts.value[breakpoint] as DashboardWidget[];
  if (!layoutToCopy) {
    return;
  }

  const zeroXWidgets = layout.value.filter((widget) => widget.x === 0);
  const maxY = zeroXWidgets.reduce((max, widget) => {
    return Math.max(max, widget.y + widget.h);
  }, 0);

  layout.value = [
    ...layout.value,
    ...layoutToCopy.map((widget, index) => ({
      ...widget,
      i: randomUUID(),
      y: maxY + index + 1,
    })),
  ];
}
</script>
<template>
  <div class="page-header py-1.5">
    <h2 class="page-header__title">
      <IconDashboard class="mr-2 self-center" />
      <span class="page-header__title-text">
        {{ $t("core.dashboard_designer.title") }}
      </span>
    </h2>
    <div
      class="hidden sm:block"
      :class="{ '!cursor-progress opacity-50': isLoading }"
    >
      <VTabbar
        :active-id="selectBreakpoint"
        :items="deviceOptions as any"
        type="outline"
        @change="handleBreakpointChange"
      ></VTabbar>
    </div>
    <div class="page-header__actions">
      <VButton ghost @click="handleBack">
        {{ $t("core.common.buttons.back") }}
      </VButton>
      <VDropdown>
        <VButton>
          <template #icon>
            <IconAddCircle />
          </template>
          {{ $t("core.dashboard_designer.actions.add_widget") }}
        </VButton>
        <template #popper>
          <VDropdownItem @click="widgetsHubModalVisible = true">
            <template #prefix-icon>
              <RiBox3Line />
            </template>
            {{
              $t("core.dashboard_designer.operations.open_widgets_hub.button")
            }}
          </VDropdownItem>
          <VDropdown :triggers="['click']" placement="left">
            <VDropdownItem>
              <template #prefix-icon>
                <RiFileCopyLine />
              </template>
              {{
                $t("core.dashboard_designer.operations.copy_from_layout.button")
              }}
            </VDropdownItem>
            <template #popper>
              <VDropdownItem
                v-for="item in [
                  ...deviceOptionDefinitions.filter(
                    (item) => item.id !== currentBreakpoint
                  ),
                ]"
                :key="item.id"
                @click="handleCopyFromLayout(item.id)"
              >
                <template #prefix-icon>
                  <component :is="item.icon" />
                </template>
                {{ item.text }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </template>
      </VDropdown>
      <VButton
        :disabled="!hasLayoutChanged"
        type="secondary"
        :loading="isSubmitting"
        @click="handleSave"
      >
        <template #icon>
          <IconSave />
        </template>
        {{ $t("core.common.buttons.save") }}
      </VButton>
    </div>
  </div>

  <div class="dashboard m-4 transition-all" :style="designContainerStyles">
    <grid-layout
      ref="gridLayoutRef"
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
      :breakpoints="{ lg: 1200, md: 996, sm: 768, xs: 480 }"
      :cols="{ lg: 12, md: 12, sm: 6, xs: 4 }"
      @breakpoint-changed="onBreakpointChange"
    >
      <WidgetEditableItem
        v-for="item in layout"
        :key="item.i"
        :item="item"
        @remove="handleRemove(item)"
        @update="handleUpdate"
      />
    </grid-layout>
  </div>
  <WidgetHubModal
    v-if="widgetsHubModalVisible"
    @close="widgetsHubModalVisible = false"
    @add-widget="handleAddWidget"
  />
</template>
