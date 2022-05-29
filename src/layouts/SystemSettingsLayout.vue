<script lang="ts" setup>
import { BasicLayout } from "@/layouts";
import { VPageHeader } from "@/components/base/header";
import { VButton } from "@/components/base/button";
import { VTabbar } from "@/components/base/tabs";
import { IconSettings } from "@/core/icons";
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
        <VButton type="secondary">安装</VButton>
      </template>
    </VPageHeader>

    <div class="p-4">
      <VTabbar
        v-model:active-id="activeTab"
        :items="SettingTabs"
        type="outline"
        @change="handleTabChange"
      ></VTabbar>

      <div>
        <RouterView></RouterView>
      </div>
    </div>
  </BasicLayout>
</template>
