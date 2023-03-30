<script lang="ts" setup>
import {
  IconArrowRight,
  IconBookRead,
  IconFolder,
  IconPages,
  IconPlug,
  IconUserSettings,
  IconPalette,
  IconWindowLine,
  IconSearch,
  VCard,
  IconUserLine,
  Dialog,
  Toast,
} from "@halo-dev/components";
import { markRaw, ref, type Component } from "vue";
import { useRouter } from "vue-router";
import ThemePreviewModal from "@/modules/interface/themes/components/preview/ThemePreviewModal.vue";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";

interface Action {
  icon: Component;
  title: string;
  action: () => void;
  permissions?: string[];
}

const router = useRouter();

const themePreviewVisible = ref(false);

const { t } = useI18n();

const actions: Action[] = [
  {
    icon: markRaw(IconUserLine),
    title: t(
      "core.dashboard.widgets.presets.quicklink.actions.user_profile.title"
    ),
    action: () => {
      router.push({
        name: "UserDetail",
        params: { name: "-" },
      });
    },
  },
  {
    icon: markRaw(IconWindowLine),
    title: t(
      "core.dashboard.widgets.presets.quicklink.actions.view_site.title"
    ),
    action: () => {
      themePreviewVisible.value = true;
    },
    permissions: ["system:themes:view"],
  },
  {
    icon: markRaw(IconBookRead),
    title: t("core.dashboard.widgets.presets.quicklink.actions.new_post.title"),
    action: () => {
      router.push({
        name: "PostEditor",
      });
    },
    permissions: ["system:posts:manage"],
  },
  {
    icon: markRaw(IconPages),
    title: t("core.dashboard.widgets.presets.quicklink.actions.new_page.title"),
    action: () => {
      router.push({
        name: "SinglePageEditor",
      });
    },
    permissions: ["system:singlepages:manage"],
  },
  {
    icon: markRaw(IconFolder),
    title: t(
      "core.dashboard.widgets.presets.quicklink.actions.upload_attachment.title"
    ),
    action: () => {
      router.push({
        name: "Attachments",
        query: {
          action: "upload",
        },
      });
    },
    permissions: ["system:attachments:manage"],
  },
  {
    icon: markRaw(IconPalette),
    title: t(
      "core.dashboard.widgets.presets.quicklink.actions.theme_manage.title"
    ),
    action: () => {
      router.push({
        name: "ThemeDetail",
      });
    },
    permissions: ["system:themes:view"],
  },
  {
    icon: markRaw(IconPlug),
    title: t(
      "core.dashboard.widgets.presets.quicklink.actions.plugin_manage.title"
    ),
    action: () => {
      router.push({
        name: "Plugins",
      });
    },
    permissions: ["system:plugins:view"],
  },
  {
    icon: markRaw(IconUserSettings),
    title: t("core.dashboard.widgets.presets.quicklink.actions.new_user.title"),
    action: () => {
      router.push({
        name: "Users",
        query: {
          action: "create",
        },
      });
    },
    permissions: ["system:users:manage"],
  },
  {
    icon: markRaw(IconSearch),
    title: t(
      "core.dashboard.widgets.presets.quicklink.actions.refresh_search_engine.title"
    ),
    action: () => {
      Dialog.warning({
        title: t(
          "core.dashboard.widgets.presets.quicklink.actions.refresh_search_engine.dialog_title"
        ),
        description: t(
          "core.dashboard.widgets.presets.quicklink.actions.refresh_search_engine.dialog_content"
        ),
        confirmText: t("core.common.buttons.confirm"),
        cancelText: t("core.common.buttons.cancel"),
        onConfirm: async () => {
          await apiClient.indices.buildPostIndices();
          Toast.success(
            t(
              "core.dashboard.widgets.presets.quicklink.actions.refresh_search_engine.success_message"
            )
          );
        },
      });
    },
    permissions: ["system:posts:manage"],
  },
];
</script>
<template>
  <VCard
    :body-class="['h-full', '@container', '!p-0', '!overflow-auto']"
    class="h-full"
    :title="$t('core.dashboard.widgets.presets.quicklink.title')"
  >
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
        <div
          v-for="(action, index) in actions"
          :key="index"
          v-permission="action.permissions"
          class="group relative cursor-pointer rounded-lg bg-gray-50 p-4 transition-all hover:bg-gray-100"
          @click="action.action"
        >
          <div>
            <span
              class="inline-flex rounded-lg bg-teal-50 p-3 text-teal-700 ring-4 ring-white"
            >
              <component :is="action.icon"></component>
            </span>
          </div>
          <div class="mt-8">
            <h3 class="text-sm font-semibold">
              {{ action.title }}
            </h3>
          </div>
          <span
            aria-hidden="true"
            class="pointer-events-none absolute right-6 top-6 text-gray-300 transition-all group-hover:translate-x-1 group-hover:text-gray-400"
          >
            <IconArrowRight />
          </span>
        </div>
      </div>
    </OverlayScrollbarsComponent>
  </VCard>
  <ThemePreviewModal
    v-model:visible="themePreviewVisible"
    :title="
      $t('core.dashboard.widgets.presets.quicklink.actions.view_site.title')
    "
  />
</template>
