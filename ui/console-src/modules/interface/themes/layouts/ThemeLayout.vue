<script lang="ts" setup>
import BasicLayout from "@console/layouts/BasicLayout.vue";
import { useThemeStore } from "@console/stores/theme";
import type { Setting, Theme } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  IconExchange,
  IconEye,
  IconListSettings,
  IconPalette,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
  VTabbar,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { storeToRefs } from "pinia";
import { computed, provide, ref, watch, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import ThemePreviewModal from "../components/preview/ThemePreviewModal.vue";
import ThemeListModal from "../components/ThemeListModal.vue";
import { useInstalledThemes } from "../composables/use-installed-themes";
import { useThemeLifeCycle } from "../composables/use-theme";

const { t } = useI18n();
const route = useRoute();
const router = useRouter();

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
    label: t("core.theme.tabs.detail"),
    route: {
      name: "ThemeDetail",
    },
  },
];

const { activatedTheme } = storeToRefs(useThemeStore());
const hasThemeQuery = computed(() => route.query.theme !== undefined);
const themeName = computed(() =>
  typeof route.query.theme === "string" ? route.query.theme : undefined
);
const {
  data: themes,
  isInitialLoading: themesLoading,
  isError: themesError,
  refetch: refetchThemes,
} = useInstalledThemes(computed(() => !!themeName.value));
const selectedTheme = computed(() => {
  if (!hasThemeQuery.value) {
    return activatedTheme.value;
  }
  return themes.value?.find(
    (theme) =>
      theme.metadata.name === themeName.value &&
      !theme.metadata.deletionTimestamp
  );
});
const themesModal = ref(false);
const themeListModal = ref<InstanceType<typeof ThemeListModal>>();
const previewModal = ref(false);
const activeTab = computed(() =>
  route.name === "ThemeSetting" ? String(route.params.group) : initialTabs[0].id
);
provide<Ref<string>>("activeTab", activeTab);
provide<Ref<boolean>>("themesModal", themesModal);

const { isActivated, handleActiveTheme } = useThemeLifeCycle(selectedTheme);

provide<Ref<Theme | undefined>>("selectedTheme", selectedTheme);

const {
  data: setting,
  isInitialLoading: settingLoading,
  isError: settingError,
  refetch: refetchSetting,
} = useQuery<Setting>({
  queryKey: [
    "theme-setting",
    computed(() => selectedTheme.value?.metadata.name),
    computed(() => selectedTheme.value?.spec.settingName),
  ],
  queryFn: async () => {
    const { data } = await consoleApiClient.theme.theme.fetchThemeSetting({
      name: selectedTheme.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(() => {
    return (
      !!selectedTheme.value &&
      !!selectedTheme.value.spec.settingName &&
      utils.permission.has(["system:themes:view"])
    );
  }),
});

const tabs = computed<ThemeTab[]>(() => [
  ...initialTabs,
  ...(setting.value?.spec.forms || []).map((item) => ({
    id: item.group,
    label: item.label || "",
    route: {
      name: "ThemeSetting",
      params: { group: item.group },
    },
  })),
]);

provide<Ref<Setting | undefined>>("setting", setting);

const handleTabChange = (id: string | number) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    return router.push({ ...tab.route, query: route.query, hash: route.hash });
  }
};

const onSelectTheme = async (theme: Theme | undefined) => {
  if (!theme) return;
  if (theme.metadata.name === selectedTheme.value?.metadata.name) {
    themeListModal.value?.close();
    return;
  }
  const failure = await router.replace({
    name: "ThemeDetail",
    query: { ...route.query, theme: theme.metadata.name },
  });
  if (!failure) {
    themeListModal.value?.close();
  }
};

// Wait for the selected theme's settings before validating a bookmarked group.
watch(
  [
    () => route.name,
    () => route.params.group,
    selectedTheme,
    tabs,
    settingLoading,
  ],
  () => {
    if (
      route.name === "ThemeSetting" &&
      selectedTheme.value &&
      !settingLoading.value &&
      !settingError.value &&
      !tabs.value.some(
        (tab) =>
          tab.route.name === "ThemeSetting" &&
          tab.route.params?.group === route.params.group
      )
    ) {
      void router.replace({
        name: "ThemeDetail",
        query: route.query,
        hash: route.hash,
      });
    }
  },
  { immediate: true }
);
</script>
<template>
  <BasicLayout>
    <VPageHeader :title="selectedTheme?.spec.displayName">
      <template #icon>
        <IconPalette />
      </template>
      <template #actions>
        <VButton
          v-if="selectedTheme && !isActivated"
          v-permission="['system:themes:manage']"
          size="sm"
          type="primary"
          @click="handleActiveTheme(true)"
        >
          {{ $t("core.common.buttons.activate") }}
        </VButton>
        <VButton
          v-if="selectedTheme"
          type="default"
          size="sm"
          @click="previewModal = true"
        >
          <template #icon>
            <IconEye />
          </template>
          {{ $t("core.common.buttons.preview") }}
        </VButton>
        <VButton type="secondary" @click="themesModal = true">
          <template #icon>
            <IconListSettings />
          </template>
          {{ $t("core.theme.actions.management") }}
        </VButton>
      </template>
    </VPageHeader>

    <div class="m-0 md:m-4">
      <VLoading v-if="themeName && themesLoading" />
      <VEmpty
        v-else-if="themeName && themesError && !themes"
        :title="$t('core.common.status.loading_error')"
      >
        <template #actions>
          <VButton @click="refetchThemes()">
            {{ $t("core.common.buttons.retry") }}
          </VButton>
        </template>
      </VEmpty>
      <VEmpty
        v-else-if="!selectedTheme"
        :message="$t('core.theme.empty.message')"
        :title="
          $t(
            hasThemeQuery
              ? 'core.common.toast.not_found'
              : 'core.theme.empty.title'
          )
        "
      >
        <template #actions>
          <VSpace>
            <VButton @click="themesModal = true">
              {{ $t("core.theme.common.buttons.install") }}
            </VButton>
            <VButton type="secondary" @click="themesModal = true">
              <template #icon>
                <IconExchange />
              </template>
              {{ $t("core.theme.empty.actions.switch") }}
            </VButton>
          </VSpace>
        </template>
      </VEmpty>

      <div v-else>
        <VCard :body-class="['!p-0', '!overflow-visible']">
          <template #header>
            <VTabbar
              :active-id="activeTab"
              :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
              class="w-full !rounded-none"
              type="outline"
              @change="handleTabChange"
            ></VTabbar>
          </template>
          <div class="rounded-b-base bg-white">
            <VEmpty
              v-if="settingError && !setting"
              :title="$t('core.common.status.loading_error')"
            >
              <template #actions>
                <VButton @click="refetchSetting()">{{
                  $t("core.common.buttons.retry")
                }}</VButton>
              </template>
            </VEmpty>
            <VLoading v-if="route.name === 'ThemeSetting' && settingLoading" />
            <RouterView
              v-if="
                route.name !== 'ThemeSetting' ||
                (!settingLoading && !(settingError && !setting))
              "
              v-slot="{ Component }"
            >
              <template v-if="Component">
                <Suspense :key="selectedTheme.metadata.name">
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
    </div>

    <ThemeListModal
      v-if="themesModal"
      ref="themeListModal"
      @close="themesModal = false"
      @select="onSelectTheme"
    />
    <ThemePreviewModal
      v-if="previewModal"
      :theme="selectedTheme"
      @close="previewModal = false"
    />
  </BasicLayout>
</template>
