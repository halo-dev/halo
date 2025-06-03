<script setup lang="ts">
import { usePermission } from "@/utils/permission";
import { VButton, VModal } from "@halo-dev/components";
import type { DashboardWidgetDefinition } from "packages/shared/dist";
import { computed, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import { useDashboardExtensionPoint } from "../composables/use-dashboard-extension-point";
import { internalWidgetDefinitions } from "../widgets";

const { t } = useI18n();
const { currentUserHasPermission } = usePermission();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "add-widget", widget: DashboardWidgetDefinition): void;
}>();

// const widgetsGroup = [
//   {
//     id: "post",
//     label: t("core.dashboard.widgets.groups.post"),
//     widgets: [
//       {
//         x: 0,
//         y: 0,
//         w: 3,
//         h: 3,
//         i: 0,
//         componentName: "PostStatsWidget",
//       },
//       {
//         x: 0,
//         y: 0,
//         w: 6,
//         h: 10,
//         i: 1,
//         componentName: "RecentPublishedWidget",
//         permissions: ["system:posts:view"],
//       },
//     ],
//   },
//   {
//     id: "page",
//     label: t("core.dashboard.widgets.groups.page"),
//     widgets: [
//       {
//         x: 0,
//         y: 0,
//         w: 3,
//         h: 3,
//         i: 0,
//         componentName: "SinglePageStatsWidget",
//         permissions: ["system:singlepages:view"],
//       },
//     ],
//   },
//   {
//     id: "comment",
//     label: t("core.dashboard.widgets.groups.comment"),
//     widgets: [
//       { x: 0, y: 0, w: 3, h: 3, i: 0, componentName: "CommentStatsWidget" },
//     ],
//   },
//   {
//     id: "user",
//     label: t("core.dashboard.widgets.groups.user"),
//     widgets: [
//       { x: 0, y: 0, w: 3, h: 3, i: 0, componentName: "UserStatsWidget" },
//     ],
//   },
//   {
//     id: "other",
//     label: t("core.dashboard.widgets.groups.other"),
//     widgets: [
//       { x: 0, y: 0, w: 3, h: 3, i: 0, componentName: "ViewsStatsWidget" },
//       { x: 0, y: 0, w: 6, h: 10, i: 1, componentName: "QuickLinkWidget" },
//       { x: 0, y: 0, w: 6, h: 10, i: 2, componentName: "NotificationWidget" },
//     ],
//   },
// ];

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const activeId = ref("dashboard");

const { widgetDefinitions } = useDashboardExtensionPoint();

const availableWidgetDefinitions = computed(() => {
  return [...internalWidgetDefinitions, ...widgetDefinitions.value];
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
    <!-- <VTabbar
      v-model:active-id="activeId"
      :items="
        widgetsGroup.map((group) => {
          return { id: group.id, label: group.label };
        })
      "
      type="outline"
    ></VTabbar> -->
    <div class="mt-4 flex flex-col gap-5">
      <div
        v-for="(item, index) in availableWidgetDefinitions"
        :key="index"
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
    </div>
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
