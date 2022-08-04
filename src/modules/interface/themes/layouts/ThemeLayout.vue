<script lang="ts" setup>
import {
  IconExchange,
  IconEye,
  IconPalette,
  VButton,
  VCard,
  VPageHeader,
  VSpace,
  VTabbar,
} from "@halo-dev/components";
import type {
  FormKitSetting,
  FormKitSettingSpec,
} from "@halo-dev/admin-shared";
import { apiClient, BasicLayout } from "@halo-dev/admin-shared";
import ThemeListModal from "../components/ThemeListModal.vue";
import type { ComputedRef, Ref } from "vue";
import { provide, ref, watch, watchEffect } from "vue";
import type { RouteLocationRaw } from "vue-router";
import { useRoute, useRouter } from "vue-router";
import type { ConfigMap, Theme } from "@halo-dev/api-client";
import { useThemeLifeCycle } from "../composables/use-theme";
import cloneDeep from "lodash.clonedeep";

interface ThemeTab {
  id: string;
  label: string;
  route: RouteLocationRaw;
}

const initialTabs: ThemeTab[] = [
  {
    id: "detail",
    label: "详情",
    route: {
      name: "ThemeDetail",
    },
  },
];

const initialConfigMap: ConfigMap = {
  data: {},
  apiVersion: "v1alpha1",
  kind: "ConfigMap",
  metadata: {
    name: "",
  },
};

const tabs = ref<ThemeTab[]>(cloneDeep(initialTabs));
const selectedTheme = ref<Theme>({} as Theme);
const settings = ref<FormKitSetting | undefined>();
const configmapFormData = ref<
  Record<string, Record<string, string>> | undefined
>();
const configmap = ref<ConfigMap>(cloneDeep(initialConfigMap));
const themesModal = ref(false);
const activeTab = ref("detail");

const { isActivated, activatedTheme, handleActiveTheme } =
  useThemeLifeCycle(selectedTheme);

provide<Ref<Theme>>("activatedTheme", activatedTheme);
provide<Ref<Theme>>("selectedTheme", selectedTheme);
provide<Ref<FormKitSetting | undefined>>("settings", settings);
provide<Ref<Record<string, Record<string, string>> | undefined>>(
  "configmapFormData",
  configmapFormData
);
provide<Ref<ConfigMap>>("configmap", configmap);
provide<ComputedRef<boolean>>("isActivated", isActivated);
provide<Ref<string | undefined>>("activeTab", activeTab);

const route = useRoute();
const router = useRouter();

const handleTabChange = (id: string) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    router.push(tab.route);
  }
};

const handleFetchSettings = async () => {
  tabs.value = cloneDeep(initialTabs);
  if (!selectedTheme.value?.spec?.settingName) {
    return;
  }
  try {
    const response = await apiClient.extension.setting.getv1alpha1Setting(
      selectedTheme.value.spec.settingName as string
    );
    settings.value = response.data as FormKitSetting;

    const { spec } = settings.value;

    if (spec) {
      tabs.value = [
        ...tabs.value,
        ...spec.map((item: FormKitSettingSpec) => {
          return {
            id: item.group,
            label: item.label || "",
            route: {
              name: "ThemeSetting",
              params: {
                group: item.group,
              },
            },
          };
        }),
      ] as ThemeTab[];

      onTabChange(route.name as string);
    }
  } catch (e) {
    console.error(e);
  }
};

const handleFetchConfigMap = async () => {
  if (!selectedTheme.value.spec?.configMapName) {
    return;
  }
  try {
    const response = await apiClient.extension.configMap.getv1alpha1ConfigMap(
      selectedTheme.value.spec?.configMapName as string
    );
    configmap.value = response.data;

    const { data } = configmap.value;

    if (data) {
      configmapFormData.value = Object.keys(data).reduce((acc, key) => {
        acc[key] = JSON.parse(data[key]);
        return acc;
      }, {});
    }
  } catch (e) {
    console.error(e);
  } finally {
    if (!configmapFormData.value) {
      configmapFormData.value = settings.value?.spec.reduce((acc, item) => {
        acc[item.group] = {};
        return acc;
      }, {});
    }
  }
};

provide<() => void>("handleFetchSettings", handleFetchSettings);
provide<() => void>("handleFetchConfigMap", handleFetchConfigMap);

watchEffect(() => {
  if (selectedTheme.value) {
    handleFetchSettings();
    handleFetchConfigMap();
  }
});

const onTabChange = (routeName: string) => {
  if (routeName === "ThemeSetting") {
    const tab = tabs.value.find((tab) => {
      return (
        // @ts-ignore
        tab.route.name === routeName &&
        // @ts-ignore
        tab.route.params.group === route.params.group
      );
    });
    if (tab) {
      activeTab.value = tab.id;
      return;
    }
    router.push({ name: "ThemeDetail" });
    return;
  }

  // @ts-ignore
  const tab = tabs.value.find((tab) => tab.route.name === route.name);
  activeTab.value = tab ? tab.id : tabs.value[0].id;
};

watch(
  () => route.name,
  async (newRouteName) => {
    onTabChange(newRouteName as string);
  }
);
</script>
<template>
  <BasicLayout>
    <ThemeListModal
      v-model:activated-theme="activatedTheme"
      v-model:selected-theme="selectedTheme"
      v-model:visible="themesModal"
    />
    <VPageHeader :title="selectedTheme.spec?.displayName">
      <template #icon>
        <IconPalette class="mr-2 self-center" />
      </template>
      <template #actions>
        <VSpace>
          <VButton size="sm" type="default" @click="themesModal = true">
            <template #icon>
              <IconExchange class="h-full w-full" />
            </template>
            切换主题
          </VButton>
          <VButton
            v-if="!isActivated"
            size="sm"
            type="primary"
            @click="handleActiveTheme"
          >
            启用
          </VButton>
          <VButton :route="{ name: 'ThemeVisual' }" type="secondary">
            <template #icon>
              <IconEye class="h-full w-full" />
            </template>
            可视化编辑
          </VButton>
        </VSpace>
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
