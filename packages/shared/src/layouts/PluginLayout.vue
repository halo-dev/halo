<script lang="ts" setup>
// core libs
import { computed, onMounted, provide, ref, watch } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import { apiClient } from "../utils/api-client";

// libs
import cloneDeep from "lodash.clonedeep";

// hooks
import { useSettingForm } from "../composables";

// components
import { VButton, VCard, VPageHeader, VTabbar } from "@halo-dev/components";
import { BasicLayout } from "../layouts";

// types
import type { Ref } from "vue";
import type { Plugin } from "@halo-dev/api-client";
import type { FormKitSettingSpec } from "../types/formkit";

interface PluginTab {
  id: string;
  label: string;
  route: {
    name: string;
    params?: Record<string, string>;
  };
}

const initialTabs: PluginTab[] = [
  {
    id: "detail",
    label: "详情",
    route: {
      name: "PluginDetail",
    },
  },
];

const route = useRoute();
const router = useRouter();

const plugin = ref<Plugin>({} as Plugin);
const tabs = ref<PluginTab[]>(cloneDeep(initialTabs));
const activeTab = ref<string>();

provide<Ref<Plugin>>("plugin", plugin);
provide<Ref<string | undefined>>("activeTab", activeTab);

const settingName = computed(() => plugin.value.spec?.settingName);
const configMapName = computed(() => plugin.value.spec?.configMapName);

const { settings, handleFetchSettings } = useSettingForm(
  settingName,
  configMapName
);

const handleFetchPlugin = async () => {
  try {
    const response =
      await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin({
        name: route.params.name as string,
      });
    plugin.value = response.data;
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

const onTabChange = (routeName: string) => {
  if (routeName === "PluginSetting") {
    const tab = tabs.value.find((tab) => {
      return (
        tab.route.name === routeName &&
        tab.route.params?.group === route.params.group
      );
    });
    if (tab) {
      activeTab.value = tab.id;
      return;
    }
    router.push({ name: "PluginDetail" });
    return;
  }
  const tab = tabs.value.find((tab) => tab.route.name === route.name);
  activeTab.value = tab ? tab.id : tabs.value[0].id;
};

onMounted(async () => {
  await handleFetchPlugin();
  await handleFetchSettings();

  tabs.value = cloneDeep(initialTabs);
  if (settings.value && settings.value.spec) {
    tabs.value = [
      ...tabs.value,
      ...settings.value.spec.map((item: FormKitSettingSpec) => {
        return {
          id: item.group,
          label: item.label || "",
          route: {
            name: "PluginSetting",
            params: {
              group: item.group,
            },
          },
        };
      }),
    ] as PluginTab[];
    onTabChange(route.name as string);
  }
});

watch(
  () => route.name,
  async (newRouteName) => {
    onTabChange(newRouteName as string);
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
