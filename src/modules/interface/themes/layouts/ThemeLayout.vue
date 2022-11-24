<script lang="ts" setup>
// core libs
import { nextTick, onMounted, type ComputedRef, type Ref } from "vue";
import { computed, provide, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

// libs
import cloneDeep from "lodash.clonedeep";

// hooks
import { useThemeLifeCycle } from "../composables/use-theme";
// types
import BasicLayout from "@/layouts/BasicLayout.vue";
import { useSettingForm } from "@/composables/use-setting-form";

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
  VLoading,
} from "@halo-dev/components";
import ThemeListModal from "../components/ThemeListModal.vue";
import ThemePreviewModal from "../components/preview/ThemePreviewModal.vue";
import type { SettingForm, Theme } from "@halo-dev/api-client";
import { usePermission } from "@/utils/permission";
import { useThemeStore } from "@/stores/theme";
import { storeToRefs } from "pinia";

const { currentUserHasPermission } = usePermission();

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
const selectedTheme = ref<Theme>();
const themesModal = ref(false);
const previewModal = ref(false);
const activeTab = ref("");

const { loading, isActivated, handleActiveTheme } =
  useThemeLifeCycle(selectedTheme);

const settingName = computed(() => selectedTheme.value?.spec.settingName);
const configMapName = computed(() => selectedTheme.value?.spec.configMapName);

const { setting, handleFetchSettings } = useSettingForm(
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

      if (!currentUserHasPermission(["system:settings:view"])) {
        handleTriggerTabChange();
        return;
      }

      await handleFetchSettings();

      if (setting.value) {
        const { forms } = setting.value.spec;
        tabs.value = [
          ...tabs.value,
          ...forms.map((item: SettingForm) => {
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

onMounted(() => {
  const themeStore = useThemeStore();

  const { activatedTheme } = storeToRefs(themeStore);

  selectedTheme.value = activatedTheme?.value;
});
</script>
<template>
  <BasicLayout>
    <ThemeListModal
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
            v-permission="['system:themes:manage']"
            size="sm"
            type="primary"
            @click="handleActiveTheme"
          >
            启用
          </VButton>
          <VButton type="secondary" size="sm" @click="previewModal = true">
            <template #icon>
              <IconEye class="h-full w-full" />
            </template>
            预览
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
      </div>
    </div>

    <ThemePreviewModal v-model:visible="previewModal" :theme="selectedTheme" />
  </BasicLayout>
</template>
