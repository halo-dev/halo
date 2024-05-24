<script lang="ts" setup>
import { useQuery } from "@tanstack/vue-query";
import NotificationSetting from "./NotificationSetting.vue";
import { apiClient } from "@/utils/api-client";
import { markRaw, ref } from "vue";
import type { Raw } from "vue";
import type { Component } from "vue";
import { provide } from "vue";
import { VTabbar } from "@halo-dev/components";
import { computed } from "vue";
import type { ComputedRef } from "vue";
import type { NotifierDescriptor } from "packages/api-client/dist";

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
      await apiClient.extension.notifierDescriptors.listNotificationHaloRunV1alpha1NotifierDescriptor();
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
