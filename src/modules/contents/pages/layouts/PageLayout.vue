<script lang="ts" setup>
import { ref, watchEffect } from "vue";
import {
  VCard,
  IconAddCircle,
  VPageHeader,
  VTabbar,
  IconPages,
  VButton,
} from "@halo-dev/components";
import { BasicLayout } from "@halo-dev/admin-shared";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

interface PageTab {
  id: string;
  label: string;
  route: {
    name: string;
  };
}

const tabs = ref<PageTab[]>([
  {
    id: "functional",
    label: "功能页面",
    route: {
      name: "FunctionalPages",
    },
  },
  {
    id: "single",
    label: "自定义页面",
    route: {
      name: "SinglePages",
    },
  },
]);
const activeTab = ref(tabs.value[0].id);

const onTabChange = (routeName: string) => {
  const tab = tabs.value.find((tab) => {
    return tab.route.name === routeName;
  });
  if (tab) {
    activeTab.value = tab.id;
    return;
  }
  activeTab.value = tabs.value[0].id;
};

const handleTabChange = (id: string) => {
  const tab = tabs.value.find((item) => item.id === id);
  if (tab) {
    router.push(tab.route);
  }
};

watchEffect(() => {
  onTabChange(route.name as string);
});
</script>

<template>
  <BasicLayout>
    <VPageHeader title="页面">
      <template #icon>
        <IconPages class="mr-2 self-center" />
      </template>
      <template #actions>
        <VButton :route="{ name: 'SinglePageEditor' }" type="secondary">
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          新建
        </VButton>
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

        <div>
          <RouterView :key="activeTab" />
        </div>
      </VCard>
    </div>
  </BasicLayout>
</template>
