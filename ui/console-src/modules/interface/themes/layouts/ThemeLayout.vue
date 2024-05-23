<script lang="ts" setup>
// core libs
import {
  computed,
  nextTick,
  onMounted,
  provide,
  type Ref,
  ref,
  watch,
} from "vue";
import { useRoute, useRouter } from "vue-router";

// libs
import { cloneDeep } from "lodash-es";

// hooks
import { useThemeLifeCycle } from "../composables/use-theme";
// types
import BasicLayout from "@console/layouts/BasicLayout.vue";

// components
import {
  Dialog,
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
import ThemeListModal from "../components/ThemeListModal.vue";
import ThemePreviewModal from "../components/preview/ThemePreviewModal.vue";
import type { Setting, SettingForm, Theme } from "@halo-dev/api-client";
import { usePermission } from "@/utils/permission";
import { useThemeStore } from "@console/stores/theme";
import { storeToRefs } from "pinia";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";

const { currentUserHasPermission } = usePermission();
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

const tabs = ref<ThemeTab[]>(cloneDeep(initialTabs));
const selectedTheme = ref<Theme>();
const themesModal = ref(false);
const previewModal = ref(false);
const activeTab = ref(tabs.value[0].id);
provide<Ref<string>>("activeTab", activeTab);
provide<Ref<boolean>>("themesModal", themesModal);

const { loading, isActivated, handleActiveTheme } =
  useThemeLifeCycle(selectedTheme);

provide<Ref<Theme | undefined>>("selectedTheme", selectedTheme);

const { data: setting } = useQuery<Setting>({
  queryKey: ["theme-setting", selectedTheme],
  queryFn: async () => {
    const { data } = await apiClient.theme.fetchThemeSetting({
      name: selectedTheme.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(() => {
    return (
      !!selectedTheme.value &&
      !!selectedTheme.value.spec.settingName &&
      currentUserHasPermission(["system:themes:view"])
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
  },
});

provide<Ref<Setting | undefined>>("setting", setting);

const handleTabChange = (id: string | number) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    activeTab.value = tab.id;
    router.push(tab.route);
  }
};

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

const onSelectTheme = () => {
  tabs.value = cloneDeep(initialTabs);
  handleTabChange(tabs.value[0].id);
};

onMounted(() => {
  const themeStore = useThemeStore();

  const { activatedTheme } = storeToRefs(themeStore);

  selectedTheme.value = activatedTheme?.value;
});

watch([() => route.name, () => route.params], async () => {
  handleTriggerTabChange();
});

// handle remote download url from route
const remoteDownloadUrl = useRouteQuery<string | null>("remote-download-url");
onMounted(() => {
  if (remoteDownloadUrl.value) {
    Dialog.warning({
      title: t("core.theme.operations.remote_download.title"),
      description: t("core.theme.operations.remote_download.description", {
        url: remoteDownloadUrl.value,
      }),
      confirmText: t("core.common.buttons.download"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm() {
        themesModal.value = true;
      },
      onCancel() {
        remoteDownloadUrl.value = null;
      },
    });
  }
});
</script>
<template>
  <BasicLayout>
    <VPageHeader :title="selectedTheme?.spec.displayName">
      <template #icon>
        <IconPalette class="mr-2 self-center" />
      </template>
      <template #actions>
        <VSpace>
          <VButton
            v-show="!isActivated"
            v-permission="['system:themes:manage']"
            size="sm"
            type="primary"
            @click="handleActiveTheme()"
          >
            {{ $t("core.common.buttons.activate") }}
          </VButton>
          <VButton type="default" size="sm" @click="previewModal = true">
            <template #icon>
              <IconEye class="h-full w-full" />
            </template>
            {{ $t("core.common.buttons.preview") }}
          </VButton>
          <VButton type="secondary" @click="themesModal = true">
            <template #icon>
              <IconListSettings class="h-full w-full" />
            </template>
            {{ $t("core.theme.actions.management") }}
          </VButton>
        </VSpace>
      </template>
    </VPageHeader>

    <div class="m-0 md:m-4">
      <VEmpty
        v-if="!selectedTheme && !loading"
        :message="$t('core.theme.empty.message')"
        :title="$t('core.theme.empty.title')"
      >
        <template #actions>
          <VSpace>
            <VButton @click="themesModal = true">
              {{ $t("core.theme.common.buttons.install") }}
            </VButton>
            <VButton type="primary" @click="themesModal = true">
              <template #icon>
                <IconExchange class="h-full w-full" />
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
              v-model:active-id="activeTab"
              :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
              class="w-full !rounded-none"
              type="outline"
              @change="handleTabChange"
            ></VTabbar>
          </template>
          <div class="rounded-b-base bg-white">
            <RouterView
              :key="`${selectedTheme?.metadata.name}-${activeTab}`"
              v-slot="{ Component }"
            >
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
    </div>

    <ThemeListModal
      v-if="themesModal"
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
