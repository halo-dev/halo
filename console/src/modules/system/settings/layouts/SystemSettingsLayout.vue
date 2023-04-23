<script lang="ts" setup>
// core libs
import { nextTick, ref, watch, type Ref, provide } from "vue";
import { useRoute, useRouter } from "vue-router";

// types
import BasicLayout from "@/layouts/BasicLayout.vue";

// components
import {
  VCard,
  VPageHeader,
  VTabbar,
  IconSettings,
  VLoading,
} from "@halo-dev/components";
import type { Setting, SettingForm } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

interface SettingTab {
  id: string;
  label: string;
  route: {
    name: string;
    params?: Record<string, string>;
  };
}

const tabs = ref<SettingTab[]>([
  {
    id: "loading",
    label: t("core.common.status.loading"),
    route: { name: "SystemSetting" },
  },
]);
const activeTab = ref(tabs.value[0].id);

const route = useRoute();
const router = useRouter();

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
          route: {
            name: "SystemSetting",
            params: {
              group: item.group,
            },
          },
        };
      });
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

  activeTab.value = tabs.value[0].id;
};

watch([() => route.name, () => route.params], async () => {
  handleTriggerTabChange();
});
</script>
<template>
  <BasicLayout>
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
