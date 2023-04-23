<script lang="ts" setup>
// core libs
import { nextTick, onMounted, type Ref, computed, watch } from "vue";
import { provide, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

// libs
import cloneDeep from "lodash.clonedeep";

// hooks
import { useThemeLifeCycle } from "../composables/use-theme";
// types
import BasicLayout from "@/layouts/BasicLayout.vue";

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
import type { Setting, SettingForm, Theme } from "@halo-dev/api-client";
import { usePermission } from "@/utils/permission";
import { useThemeStore } from "@/stores/theme";
import { storeToRefs } from "pinia";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";
import { useQuery } from "@tanstack/vue-query";

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

const handleTabChange = (id: string) => {
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
</script>
<template>
  <BasicLayout>
    <ThemeListModal
      v-model:selected-theme="selectedTheme"
      v-model:visible="themesModal"
      @select="onSelectTheme"
    />
    <VPageHeader :title="selectedTheme?.spec.displayName">
      <template #icon>
        <IconPalette class="mr-2 self-center" />
      </template>
      <template #actions>
        <VSpace>
          <VButton
            v-if="!isActivated"
            v-permission="['system:themes:manage']"
            size="sm"
            type="primary"
            @click="handleActiveTheme()"
          >
            {{ $t("core.common.buttons.active") }}
          </VButton>
          <VButton type="secondary" size="sm" @click="previewModal = true">
            <template #icon>
              <IconEye class="h-full w-full" />
            </template>
            {{ $t("core.common.buttons.preview") }}
          </VButton>
          <VButton size="sm" type="default" @click="themesModal = true">
            <template #icon>
              <IconExchange class="h-full w-full" />
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

    <ThemePreviewModal v-model:visible="previewModal" :theme="selectedTheme" />
  </BasicLayout>
</template>
