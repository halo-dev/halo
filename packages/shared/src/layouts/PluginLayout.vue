<script lang="ts" setup>
import { VButton, VCard, VPageHeader, VTabbar } from "@halo-dev/components";
import type { RouteLocationRaw } from "vue-router";
import { RouterView, useRoute, useRouter } from "vue-router";
import type { Ref } from "vue";
import { onMounted, provide, ref, watch } from "vue";
import type { Plugin } from "@halo-dev/api-client";
import type { FormKitSetting, FormKitSettingSpec } from "@/types/formkit";
import { BasicLayout } from "../layouts";
import { apiClient } from "@/utils/api-client";

interface PluginTab {
  id: string;
  label: string;
  route: RouteLocationRaw;
}

const route = useRoute();
const router = useRouter();

const plugin = ref<Plugin>({} as Plugin);
const tabs = ref<PluginTab[]>([
  {
    id: "detail",
    label: "详情",
    route: {
      name: "PluginDetail",
    },
  },
]);
const activeTab = ref<string>();

provide<Ref<Plugin>>("plugin", plugin);
provide<Ref<string | undefined>>("activeTab", activeTab);

const handleFetchPlugin = async () => {
  try {
    const response =
      await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin(
        route.params.name as string
      );
    plugin.value = response.data;

    await handleFetchSettings();
  } catch (e) {
    console.error(e);
  }
};

const handleFetchSettings = async () => {
  if (!plugin.value.spec.settingName) {
    return;
  }
  try {
    const response = await apiClient.extension.setting.getv1alpha1Setting(
      plugin.value.spec.settingName as string
    );
    const settings = response.data as FormKitSetting;

    const { spec } = settings;

    if (spec) {
      spec.forEach((item: FormKitSettingSpec) => {
        tabs.value.push({
          id: item.group,
          label: item.label || "",
          route: {
            name: "PluginSetting",
            params: {
              group: item.group,
            },
          },
        });
      });
    }
  } catch (e) {
    console.error(e);
  }
};

const handleTabChange = (id: string) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    router.push(tab.route);
  }
};

onMounted(async () => {
  await handleFetchPlugin();

  // @ts-ignore
  const tab = tabs.value.find((tab) => tab.route.name === route.name);
  activeTab.value = tab ? tab.id : tabs.value[0].id;
});

watch(
  () => route.name,
  async (newRouteName) => {
    // @ts-ignore
    const tab = tabs.value.find((tab) => tab.route.name === newRouteName);
    activeTab.value = tab ? tab.id : tabs.value[0].id;
  }
);
</script>
<template>
  <BasicLayout>
    <VPageHeader :title="plugin?.spec?.displayName">
      <template #icon>
        <img :src="plugin?.spec?.logo" class="mr-2 h-8 w-8" />
      </template>
      <template #actions>
        <VButton class="opacity-0" type="secondary">安装</VButton>
      </template>
    </VPageHeader>

    <div class="m-0 md:m-4">
      <VCard :body-class="['!p-0']">
        <template #header>
          <VTabbar
            v-model:active-id="activeTab"
            :items="tabs"
            class="w-full !rounded-none"
            type="outline"
            @change="handleTabChange"
          ></VTabbar>
        </template>
      </VCard>
      <div>
        <RouterView :key="activeTab" />
      </div>
    </div>
  </BasicLayout>
</template>
