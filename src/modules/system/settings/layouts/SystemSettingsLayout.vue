<script lang="ts" setup>
// core libs
import { nextTick, onMounted } from "vue";
import { ref, watch } from "vue";
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

const { settings, handleFetchSettings } = useSettingForm(
  ref("system"),
  ref("system")
);

const route = useRoute();
const router = useRouter();

const handleTabChange = (id: string) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    activeTab.value = tab.id;
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
  }

  await nextTick();

  handleTriggerTabChange();
});

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
  </BasicLayout>
</template>
