<script lang="ts" setup>
import type { Plugin, Setting } from "@halo-dev/api-client";
import { VAvatar, VCard, VPageHeader, VTabbar } from "@halo-dev/components";
import type { Ref } from "vue";
import { provide, toRefs } from "vue";
import { useRoute, useRouter } from "vue-router";
import { usePluginDetailTabs } from "./composables/use-plugin";

const route = useRoute();
const router = useRouter();

const { name } = toRefs(route.params);

const { plugin, setting, activeTab, tabs } = usePluginDetailTabs(
  name as Ref<string | undefined>,
  true
);

provide<Ref<string>>("activeTab", activeTab);
provide<Ref<Plugin | undefined>>("plugin", plugin);
provide<Ref<Setting | undefined>>("setting", setting);

async function handleTabChange(id: string | number) {
  await router.push({
    query: {
      ...route.query,
      tab: String(id),
    },
  });
}
</script>
<template>
  <VPageHeader :title="plugin?.spec?.displayName">
    <template #icon>
      <VAvatar
        v-if="plugin"
        :src="plugin.status?.logo"
        :alt="plugin.spec.displayName"
        size="sm"
      />
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
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
        <template v-for="tab in tabs" :key="tab.id">
          <component :is="tab.component" v-if="activeTab === tab.id" />
        </template>
      </div>
    </VCard>
  </div>
</template>
