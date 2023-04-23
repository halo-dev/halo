<script lang="ts" setup>
// core libs
import { nextTick, provide, ref, computed, watch } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import { apiClient } from "@/utils/api-client";

// libs
import cloneDeep from "lodash.clonedeep";

// components
import {
  VCard,
  VPageHeader,
  VTabbar,
  VAvatar,
  VLoading,
} from "@halo-dev/components";
import BasicLayout from "@/layouts/BasicLayout.vue";

// types
import type { Ref } from "vue";
import type { Plugin, Setting, SettingForm } from "@halo-dev/api-client";
import { usePermission } from "@/utils/permission";
import { useI18n } from "vue-i18n";
import { useQuery } from "@tanstack/vue-query";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

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
    label: t("core.plugin.tabs.detail"),
    route: {
      name: "PluginDetail",
    },
  },
];

const route = useRoute();
const router = useRouter();

const tabs = ref<PluginTab[]>(cloneDeep(initialTabs));
const activeTab = ref<string>(tabs.value[0].id);

provide<Ref<string | undefined>>("activeTab", activeTab);

const { data: plugin } = useQuery({
  queryKey: ["plugin", route.params.name],
  queryFn: async () => {
    const { data } =
      await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin({
        name: route.params.name as string,
      });
    return data;
  },
});

provide<Ref<Plugin | undefined>>("plugin", plugin);

const { data: setting } = useQuery({
  queryKey: ["plugin-setting", plugin],
  queryFn: async () => {
    const { data } = await apiClient.plugin.fetchPluginSetting({
      name: plugin.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(() => {
    return (
      !!plugin.value &&
      !!plugin.value.spec.settingName &&
      currentUserHasPermission(["system:plugins:manage"])
    );
  }),
  async onSuccess(data) {
    if (data) {
      const { forms } = data.spec;
      tabs.value = [
        ...initialTabs,
        ...forms.map((item: SettingForm) => {
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
    }

    await nextTick();

    handleTriggerTabChange();
  },
});

provide<Ref<Setting | undefined>>("setting", setting);

const handleTabChange = (id: string) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    activeTab.value = tab.id;
    router.push(tab.route);
  }
};

const handleTriggerTabChange = () => {
  if (route.name === "PluginSetting") {
    const tab = tabs.value.find((tab) => {
      return (
        tab.route.name === route.name &&
        tab.route.params?.group === route.params.group
      );
    });
    if (tab) {
      activeTab.value = tab.id;
      return;
    }
    handleTabChange(tabs.value[0].id);
    return;
  }
  const tab = tabs.value.find((tab) => tab.route.name === route.name);
  activeTab.value = tab ? tab.id : tabs.value[0].id;
};

watch([() => route.name, () => route.params], () => {
  handleTriggerTabChange();
});
</script>
<template>
  <BasicLayout>
    <VPageHeader :title="plugin?.spec?.displayName">
      <template #icon>
        <VAvatar
          v-if="plugin"
          :src="plugin.status?.logo"
          :alt="plugin.spec.displayName"
          class="mr-2"
          size="sm"
        />
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
        <div class="bg-white">
          <RouterView :key="activeTab" v-slot="{ Component }">
            <template v-if="Component">
              <Suspense>
                <component :is="Component"></component>
                <template #fallback>
                  <VLoading />
                </template>
              </Suspense>
            </template>
          </RouterView>
        </div>
      </VCard>
    </div>
  </BasicLayout>
</template>
