<script lang="ts" setup>
// core libs
import { nextTick, type ComputedRef, type Ref } from "vue";
import { computed, provide, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

// libs
import cloneDeep from "lodash.clonedeep";

// hooks
import { useThemeLifeCycle } from "../composables/use-theme";
// types
import type { FormKitSettingSpec } from "@halo-dev/admin-shared";
import { BasicLayout, useSettingForm } from "@halo-dev/admin-shared";

// components
import {
  IconExchange,
  IconEye,
  IconPalette,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VSpace,
  VTabbar,
} from "@halo-dev/components";
import ThemeListModal from "../components/ThemeListModal.vue";
import type { Theme } from "@halo-dev/api-client";

interface ThemeTab {
  id: string;
  label: string;
  route: {
    name: string;
    params?: Record<string, string>;
  };
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

const tabs = ref<ThemeTab[]>(cloneDeep(initialTabs));
const selectedTheme = ref<Theme | undefined>();
const themesModal = ref(false);
const activeTab = ref("");

const { loading, isActivated, activatedTheme, handleActiveTheme } =
  useThemeLifeCycle(selectedTheme);

const settingName = computed(() => selectedTheme.value?.spec.settingName);
const configMapName = computed(() => selectedTheme.value?.spec.configMapName);

const { settings, handleFetchSettings } = useSettingForm(
  settingName,
  configMapName
);

provide<Ref<Theme | undefined>>("selectedTheme", selectedTheme);
provide<ComputedRef<boolean>>("isActivated", isActivated);

const route = useRoute();
const router = useRouter();

const handleTabChange = (id: string) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    activeTab.value = tab.id;
    router.push(tab.route);
  }
};

watch(
  () => selectedTheme.value,
  async () => {
    if (selectedTheme.value) {
      // reset tabs
      tabs.value = cloneDeep(initialTabs);
      await handleFetchSettings();

      if (settings.value && settings.value.spec) {
        tabs.value = [
          ...tabs.value,
          ...settings.value.spec.map((item: FormKitSettingSpec) => {
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
      }

      await nextTick();

      handleTriggerTabChange();
    }
  }
);

const handleTriggerTabChange = () => {
  if (route.name === "ThemeSetting") {
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

watch([() => route.name, () => route.params], async () => {
  handleTriggerTabChange();
});
</script>
<template>
  <BasicLayout>
    <ThemeListModal
      v-model:activated-theme="activatedTheme"
      v-model:selected-theme="selectedTheme"
      v-model:visible="themesModal"
    />
    <VPageHeader :title="selectedTheme?.spec.displayName">
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
      <VEmpty
        v-if="!selectedTheme && !loading"
        message="当前没有已激活或者选择的主题，你可以切换主题或者安装新主题"
        title="当前没有已激活或已选择的主题"
      >
        <template #actions>
          <VSpace>
            <VButton @click="themesModal = true"> 安装主题 </VButton>
            <VButton type="primary" @click="themesModal = true">
              <template #icon>
                <IconExchange class="h-full w-full" />
              </template>
              切换主题
            </VButton>
          </VSpace>
        </template>
      </VEmpty>

      <div v-else>
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
          <RouterView :key="activeTab" v-slot="{ Component }">
            <template v-if="Component">
              <Suspense>
                <component :is="Component"></component>
                <template #fallback>
                  <div class="flex h-32 w-full justify-center bg-white">
                    <span class="text-sm text-gray-600">加载中...</span>
                  </div>
                </template>
              </Suspense>
            </template>
          </RouterView>
        </div>
      </div>
    </div>
  </BasicLayout>
</template>
