<script lang="ts" setup>
import {
  VPageHeader,
  VCard,
  VButton,
  Toast,
  VTabbar,
  IconAddCircle,
  IconServerLine,
} from "@halo-dev/components";

import { useQueryClient } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import type { Raw } from "vue";
import type { Component } from "vue";
import { shallowRef } from "vue";
import ListTab from "./tabs/List.vue";
import RestoreTab from "./tabs/Restore.vue";
import { useRouteQuery } from "@vueuse/router";
import { markRaw } from "vue";

const queryClient = useQueryClient();

interface BackupTab {
  id: string;
  label: string;
  component: Raw<Component>;
  permissions?: string[];
}

const tabs = shallowRef<BackupTab[]>([
  {
    id: "backups",
    label: "备份",
    component: markRaw(ListTab),
  },
  {
    id: "restore",
    label: "恢复",
    component: markRaw(RestoreTab),
  },
]);

const activeTab = useRouteQuery<string>("tab", tabs.value[0].id);

const handleCreate = async () => {
  await apiClient.extension.backup.createmigrationHaloRunV1alpha1Backup({
    backup: {
      apiVersion: "migration.halo.run/v1alpha1",
      kind: "Backup",
      metadata: {
        generateName: "backup-",
        name: "",
      },
      spec: {},
    },
  });

  queryClient.invalidateQueries({ queryKey: ["backups"] });

  Toast.success("创建成功");
};
</script>

<template>
  <VPageHeader title="备份与恢复">
    <template #icon>
      <IconServerLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton type="secondary" @click="handleCreate">
        <template #icon>
          <IconAddCircle class="h-full w-full" />
        </template>
        创建备份
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <VTabbar
          v-model:active-id="activeTab"
          :items="tabs.map((item) => ({ id: item.id, label: item.label }))"
          class="w-full !rounded-none"
          type="outline"
        ></VTabbar>
      </template>
      <div class="bg-white">
        <template v-for="tab in tabs" :key="tab.id">
          <component :is="tab.component" v-if="activeTab === tab.id" />
        </template>
      </div>
    </VCard>
  </div>
</template>
