<script lang="ts" setup>
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import type { FormKitOptionsList } from "@formkit/inputs";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconAccountCircleLine,
  IconBookRead,
  IconFolder,
  IconPages,
  IconPalette,
  IconPlug,
  IconSearch,
  IconSettings,
  IconUserSettings,
  IconWindowLine,
  Toast,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import type { DashboardWidgetQuickActionItem } from "@halo-dev/ui-shared";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { computed, markRaw, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import QuickActionItem from "./QuickActionItem.vue";
import ThemePreviewItem from "./ThemePreviewItem.vue";
import { useDashboardQuickActionExtensionPoint } from "./composables/use-dashboard-extension-point";

const props = defineProps<{
  editMode?: boolean;
  config?: {
    enabled_items?: string[];
  };
}>();

const { t } = useI18n();

const emit = defineEmits<{
  (e: "update:config", config: Record<string, unknown>): void;
}>();

const presetItems: DashboardWidgetQuickActionItem[] = [
  {
    id: "core:user-center",
    icon: markRaw(IconAccountCircleLine),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.user_center.title"
    ),
    action: () => {
      window.location.href = "/uc/profile";
    },
  },
  {
    id: "core:theme-preview",
    icon: markRaw(IconWindowLine),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.view_site.title"
    ),
    component: markRaw(ThemePreviewItem),
    permissions: ["system:themes:view"],
  },
  {
    id: "core:new-post",
    icon: markRaw(IconBookRead),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.new_post.title"
    ),
    route: {
      name: "PostEditor",
    },
    permissions: ["system:posts:manage"],
  },
  {
    id: "core:new-page",
    icon: markRaw(IconPages),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.new_page.title"
    ),
    route: {
      name: "SinglePageEditor",
    },
    permissions: ["system:singlepages:manage"],
  },
  {
    id: "core:upload-attachment",
    icon: markRaw(IconFolder),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.upload_attachment.title"
    ),
    route: {
      name: "Attachments",
      query: {
        action: "upload",
      },
    },
    permissions: ["system:attachments:manage"],
  },
  {
    id: "core:theme-manage",
    icon: markRaw(IconPalette),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.theme_manage.title"
    ),
    route: {
      name: "ThemeDetail",
    },
    permissions: ["system:themes:view"],
  },
  {
    id: "core:plugin-manage",
    icon: markRaw(IconPlug),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.plugin_manage.title"
    ),
    route: {
      name: "Plugins",
    },
    permissions: ["system:plugins:view"],
  },
  {
    id: "core:new-user",
    icon: markRaw(IconUserSettings),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.new_user.title"
    ),
    route: {
      name: "Users",
      query: {
        action: "create",
      },
    },
    permissions: ["system:users:manage"],
  },
  {
    id: "core:refresh-search-engine",
    icon: markRaw(IconSearch),
    title: t(
      "core.dashboard.widgets.presets.quickaction.actions.refresh_search_engine.title"
    ),
    action: () => {
      Dialog.warning({
        title: t(
          "core.dashboard.widgets.presets.quickaction.actions.refresh_search_engine.dialog_title"
        ),
        description: t(
          "core.dashboard.widgets.presets.quickaction.actions.refresh_search_engine.dialog_content"
        ),
        confirmText: t("core.common.buttons.confirm"),
        cancelText: t("core.common.buttons.cancel"),
        onConfirm: async () => {
          await consoleApiClient.content.indices.rebuildAllIndices();
          Toast.success(
            t(
              "core.dashboard.widgets.presets.quickaction.actions.refresh_search_engine.success_message"
            )
          );
        },
      });
    },
    permissions: ["*"],
  },
];

const { quickActionItems } = useDashboardQuickActionExtensionPoint();

const allQuickActionItems = computed(() => {
  return [...presetItems, ...quickActionItems.value];
});

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");
const configVisible = ref(false);

function onConfigFormSubmit(data: { enabled_items: string[] }) {
  emit("update:config", data);
  configVisible.value = false;
}

const itemOptions = computed(() => {
  return allQuickActionItems.value.map((item) => ({
    label: item.title,
    value: item.id,
  })) as FormKitOptionsList;
});

const availableItems = computed(() => {
  const enabledItems = props.config?.enabled_items;

  if (!enabledItems || enabledItems.length === 0) {
    return [];
  }

  const indexMap = new Map();
  enabledItems.forEach((id, index) => {
    indexMap.set(id, index);
  });
  return allQuickActionItems.value
    .filter((item) => indexMap.has(item.id))
    .sort((a, b) => indexMap.get(a.id) - indexMap.get(b.id));
});
</script>
<template>
  <WidgetCard v-bind="$attrs" :body-class="['@container', '!overflow-auto']">
    <template #title>
      <div class="inline-flex items-center gap-2">
        <div class="flex-1 shrink text-base font-medium">
          {{ $t("core.dashboard.widgets.presets.quickaction.title") }}
        </div>
        <IconSettings
          v-if="editMode"
          class="cursor-pointer hover:text-gray-600"
          @click="configVisible = true"
        />
      </div>
    </template>
    <OverlayScrollbarsComponent
      element="div"
      :options="{ scrollbars: { autoHide: 'scroll' } }"
      class="h-full w-full"
      style="padding: 12px 16px"
      defer
    >
      <div
        class="grid grid-cols-1 gap-2 overflow-hidden @sm:grid-cols-2 @md:grid-cols-3"
      >
        <template v-for="item in availableItems" :key="item.id">
          <QuickActionItem v-if="!item.component" :item="item" />
          <component :is="item.component" v-else :item="item" />
        </template>
      </div>
    </OverlayScrollbarsComponent>
  </WidgetCard>
  <VModal
    v-if="configVisible"
    ref="modal"
    mount-to-body
    :title="$t('core.dashboard_designer.config_modal.title')"
    @close="configVisible = false"
  >
    <div>
      <FormKit
        id="quick-action-widget-config"
        name="quick-action-widget-config"
        type="form"
        :preserve="true"
        @submit="onConfigFormSubmit"
      >
        <FormKit
          type="select"
          :label="
            $t(
              'core.dashboard.widgets.presets.quickaction.config.fields.enabled_items.label'
            )
          "
          name="enabled_items"
          :options="itemOptions"
          :value="config?.enabled_items || []"
          multiple
          searchable
          sortable
        />
      </FormKit>
    </div>
    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          @click="$formkit.submit('quick-action-widget-config')"
        >
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
