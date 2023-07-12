<script lang="ts" setup>
import {
  VPageHeader,
  IconLockPasswordLine,
  VCard,
  VLoading,
  IconAddCircle,
  VButton,
  Toast,
  VEmpty,
  VSpace,
} from "@halo-dev/components";

import BackupListItem from "./components/BackupListItem.vue";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";

const queryClient = useQueryClient();

const {
  data: backups,
  isLoading,
  isFetching,
  refetch,
} = useQuery({
  queryKey: ["backups"],
  queryFn: async () => {
    const { data } =
      await apiClient.extension.backup.listmigrationHaloRunV1alpha1Backup();
    return data;
  },
});

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
      <IconLockPasswordLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton type="secondary" @click="handleCreate"> 创建备份 </VButton>
        <VButton type="secondary" @click="handleCreate"> 还原 </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col items-start sm:flex-row sm:items-center"
          >
            <div class="mr-4 hidden items-center sm:flex">
              <input
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <VButton v-if="false" type="danger" disabled>
                {{ $t("core.common.buttons.delete") }}
              </VButton>
            </div>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!backups?.items?.length" appear name="fade">
        <VEmpty
          message="当前没有已创建的备份，你可以点击刷新或者创建新的备份"
          title="没有备份"
        >
          <template #actions>
            <VSpace>
              <VButton :loading="isFetching" @click="refetch()">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton type="secondary">
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                创建备份
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(backup, index) in backups?.items" :key="index">
            <BackupListItem :backup="backup" />
          </li>
        </ul>
      </Transition>
    </VCard>
  </div>
</template>
