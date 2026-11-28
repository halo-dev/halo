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

const { plugin, pluginLoading, setting, activeTab, tabs } = usePluginDetailTabs(
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
  <VPageHeader :loading="pluginLoading" :title="plugin?.spec?.displayName">
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
    <VCard flush overflow="visible">
      <template #header>
        <VTabbar
          :active-id="activeTab"
          :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
          flush
          class="w-full"
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
