<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";
import { BackupStatusPhaseEnum } from "@halo-dev/api-client";
import {
  IconAddCircle,
  VButton,
  VEmpty,
  VLoading,
  VSpace,
} from "@halo-dev/components";
import BackupListItem from "../components/BackupListItem.vue";
import { useBackup } from "../composables/use-backup";

const {
  data: backups,
  isLoading,
  isFetching,
  refetch,
} = useQuery({
  queryKey: ["backups"],
  queryFn: async () => {
    const { data } =
      await apiClient.extension.backup.listmigrationHaloRunV1alpha1Backup({
        sort: ["metadata.creationTimestamp,desc"],
      });
    return data;
  },
  refetchInterval(data) {
    const deletingBackups = data?.items.filter((backup) => {
      return !!backup.metadata.deletionTimestamp;
    });

    if (deletingBackups?.length) {
      return 1000;
    }

    const pendingBackups = data?.items.filter((backup) => {
      return (
        backup.status?.phase === BackupStatusPhaseEnum.Pending ||
        backup.status?.phase === BackupStatusPhaseEnum.Running
      );
    });

    if (pendingBackups?.length) {
      return 3000;
    }

    return false;
  },
});

const { handleCreate } = useBackup();
</script>

<template>
  <VLoading v-if="isLoading" />
  <Transition v-else-if="!backups?.items?.length" appear name="fade">
    <VEmpty
      :message="$t('core.backup.empty.message')"
      :title="$t('core.backup.empty.title')"
    >
      <template #actions>
        <VSpace>
          <VButton :loading="isFetching" @click="refetch()">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
          <VButton type="secondary" @click="handleCreate">
            <template #icon>
              <IconAddCircle class="h-full w-full" />
            </template>
            {{ $t("core.backup.empty.actions.create") }}
          </VButton>
        </VSpace>
      </template>
    </VEmpty>
  </Transition>
  <Transition v-else appear name="fade">
    <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
      <li v-for="(backup, index) in backups?.items" :key="index">
        <BackupListItem :backup="backup" />
      </li>
    </ul>
  </Transition>
</template>
