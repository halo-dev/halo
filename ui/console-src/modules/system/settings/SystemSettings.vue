<script lang="ts" setup>
// core libs
import { provide, ref, type Ref } from "vue";

// components
import { usePermission } from "@/utils/permission";
import type { Setting, SettingForm } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  IconSettings,
  VCard,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import type { Component, Raw } from "vue";
import { markRaw } from "vue";
import { useI18n } from "vue-i18n";
import NotificationsTab from "./tabs/Notifications.vue";
import SettingTab from "./tabs/Setting.vue";

const { t } = useI18n();
const { currentUserHasPermission } = usePermission();

interface Tab {
  id: string;
  label: string;
  component: Raw<Component>;
}

const tabs = ref<Tab[]>([
  {
    id: "loading",
    label: t("core.common.status.loading"),
    component: markRaw(SettingTab),
  },
]);

const activeTab = useRouteQuery<string>("tab", "basic");
provide<Ref<string>>("activeTab", activeTab);

const { data: setting } = useQuery({
  queryKey: ["system-setting"],
  queryFn: async () => {
    const { data } = await coreApiClient.setting.getSetting({
      name: "system",
    });
    return data;
  },
  async onSuccess(data) {
    if (data) {
      const { forms } = data.spec;
      tabs.value = forms.map((item: SettingForm) => {
        return {
          id: item.group,
          label: item.label || "",
          component: markRaw(SettingTab),
        };
      });

      if (!activeTab.value) {
        activeTab.value = tabs.value[0].id;
      }

      // TODO: use integrations center to refactor this
      if (currentUserHasPermission(["system:notifier:configuration"])) {
        tabs.value.push({
          id: "notification",
          label: "通知设置",
          component: markRaw(NotificationsTab),
        });
      }
    }
  },
});

provide<Ref<Setting | undefined>>("setting", setting);
</script>
<template>
  <VPageHeader :title="$t('core.setting.title')">
    <template #icon>
      <IconSettings class="mr-2 self-center" />
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0', '!overflow-visible']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTab"
          :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div class="rounded-b-base bg-white">
        <template v-for="tab in tabs" :key="tab.id">
          <component :is="tab.component" v-if="activeTab === tab.id" />
        </template>
      </div>
    </VCard>
  </div>
</template>
