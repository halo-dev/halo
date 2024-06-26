<script lang="ts" setup>
import type { NotifierDescriptor } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { VTabbar } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import type { Component, ComputedRef, Raw } from "vue";
import { computed, markRaw, provide, ref } from "vue";
import NotificationSetting from "./NotificationSetting.vue";

interface Tab {
  id: string;
  label: string;
  component: Raw<Component>;
}

const tabs = ref<Tab[]>();

const activeTab = ref();

const { data: notifierDescriptors } = useQuery({
  queryKey: ["notifier-descriptors"],
  queryFn: async () => {
    const { data } =
      await coreApiClient.notification.notifierDescriptor.listNotifierDescriptor();
    return data.items;
  },
  onSuccess(data) {
    if (data) {
      tabs.value = data.map((descriptor) => {
        return {
          id: descriptor.metadata.name,
          label: descriptor.spec?.displayName,
          component: markRaw(NotificationSetting),
        };
      }) as Tab[];

      if (!activeTab.value) {
        activeTab.value = tabs.value[0].id;
      }
    }
  },
});

const notifierDescriptor = computed(() => {
  return notifierDescriptors.value?.find(
    (item) => item.metadata.name === activeTab.value
  );
});

provide<ComputedRef<NotifierDescriptor | undefined>>(
  "notifierDescriptor",
  notifierDescriptor
);
</script>

<template>
  <div class="p-4">
    <VTabbar
      v-model:active-id="activeTab"
      :items="tabs?.map((item) => ({ id: item.id, label: item.label }))"
      class="w-full"
      type="pills"
    ></VTabbar>
    <div class="mt-4">
      <template v-for="tab in tabs" :key="tab.id">
        <component :is="tab.component" v-if="activeTab === tab.id" />
      </template>
    </div>
  </div>
</template>
