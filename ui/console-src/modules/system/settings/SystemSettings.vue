<script lang="ts" setup>
import type { Setting, SettingForm } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  IconSettings,
  VCard,
  VLoading,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import type { Component, Raw } from "vue";
import {
  defineAsyncComponent,
  markRaw,
  provide,
  shallowRef,
  type Ref,
} from "vue";
import { useI18n } from "vue-i18n";
import SettingTab from "./tabs/Setting.vue";

const { t } = useI18n();

interface Tab {
  id: string;
  label: string;
  component: Raw<Component>;
}

const tabs = shallowRef<Tab[]>([
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
      if (utils.permission.has(["system:notifier:configuration"])) {
        tabs.value = [
          ...tabs.value,
          {
            id: "notification",
            label: "通知设置",
            component: defineAsyncComponent({
              loader: () => import("./tabs/Notifications.vue"),
              loadingComponent: VLoading,
            }),
          },
        ];
      }
    }
  },
});

provide<Ref<Setting | undefined>>("setting", setting);
</script>
<template>
  <VPageHeader :title="$t('core.setting.title')">
    <template #icon>
      <IconSettings />
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
