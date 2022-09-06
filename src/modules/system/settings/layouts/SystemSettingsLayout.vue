<script lang="ts" setup>
// core libs
import { onMounted, type Ref } from "vue";
import { provide, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

// types
import type { FormKitSettingSpec } from "@halo-dev/admin-shared";
import { BasicLayout, useSettingForm } from "@halo-dev/admin-shared";

// components
import {
  VCard,
  VPageHeader,
  VTabbar,
  IconSettings,
} from "@halo-dev/components";

interface SettingTab {
  id: string;
  label: string;
  route: {
    name: string;
    params?: Record<string, string>;
  };
}

const tabs = ref<SettingTab[]>([] as SettingTab[]);
const activeTab = ref("");

const settingName = ref("system");
const configMapName = ref("system");

const { settings, handleFetchSettings } = useSettingForm(
  settingName,
  configMapName
);

provide<Ref<string | undefined>>("activeTab", activeTab);

const route = useRoute();
const router = useRouter();

const handleTabChange = (id: string) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    router.push(tab.route);
  }
};

onMounted(async () => {
  await handleFetchSettings();
  if (settings.value && settings.value.spec) {
    tabs.value = settings.value.spec.map((item: FormKitSettingSpec) => {
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
    onTabChange(route.name as string);
  }
});

const onTabChange = (routeName: string) => {
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

  activeTab.value = tabs.value[0].id;
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
    <VPageHeader title="设置">
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
      </VCard>
      <div>
        <RouterView :key="activeTab" />
      </div>
    </div>
  </BasicLayout>
</template>
