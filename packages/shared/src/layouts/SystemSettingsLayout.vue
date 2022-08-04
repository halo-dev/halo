<script lang="ts" setup>
import { BasicLayout } from "./index";
import {
  IconSettings,
  VButton,
  VCard,
  VPageHeader,
  VTabbar,
} from "@halo-dev/components";
import { onMounted, ref } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";

const SettingTabs = [
  {
    id: "general",
    label: "基本设置",
    routeName: "GeneralSettings",
  },
  {
    id: "notification",
    label: "通知设置",
    routeName: "NotificationSettings",
  },
];

const activeTab = ref();

const { name: currentRouteName } = useRoute();
const router = useRouter();

// set default active tab
onMounted(() => {
  const tab = SettingTabs.find((tab) => tab.routeName === currentRouteName);
  activeTab.value = tab ? tab.id : SettingTabs[0].id;
});

const handleTabChange = (id: string) => {
  const tab = SettingTabs.find((tab) => tab.id === id);
  if (tab) {
    router.push({ name: tab.routeName });
  }
};
</script>
<template>
  <BasicLayout>
    <VPageHeader title="设置">
      <template #icon>
        <IconSettings class="mr-2 self-center" />
      </template>
      <template #actions>
        <VButton class="opacity-0" type="secondary">安装</VButton>
      </template>
    </VPageHeader>

    <div class="m-0 md:m-4">
      <VCard>
        <template #header>
          <VTabbar
            v-model:active-id="activeTab"
            :items="SettingTabs"
            class="w-full !rounded-none"
            type="outline"
            @change="handleTabChange"
          ></VTabbar>
        </template>
        <RouterView></RouterView>
      </VCard>
    </div>
  </BasicLayout>
</template>
