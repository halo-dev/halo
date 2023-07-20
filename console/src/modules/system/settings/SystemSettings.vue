<script lang="ts" setup>
// core libs
import { ref, type Ref, provide } from "vue";

// components
import {
  VCard,
  VPageHeader,
  VTabbar,
  IconSettings,
} from "@halo-dev/components";
import type { Setting, SettingForm } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import type { Raw } from "vue";
import type { Component } from "vue";
import { markRaw } from "vue";
import SettingTab from "./tabs/Setting.vue";
import { useRouteQuery } from "@vueuse/router";

const { t } = useI18n();

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
    const { data } = await apiClient.extension.setting.getv1alpha1Setting({
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
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTab"
          :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div class="bg-white">
        <template v-for="tab in tabs" :key="tab.id">
          <component :is="tab.component" v-if="activeTab === tab.id" />
        </template>
      </div>
    </VCard>
  </div>
</template>
