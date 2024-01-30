<script lang="ts" setup>
import { VButton, VEmpty, VLoading } from "@halo-dev/components";
import BackupListItem from "../components/BackupListItem.vue";
import { useBackupFetch } from "../composables/use-backup";

const { data: backups, isLoading, isFetching, refetch } = useBackupFetch();
</script>

<template>
  <VLoading v-if="isLoading" />
  <Transition v-else-if="!backups?.items?.length" appear name="fade">
    <VEmpty
      :message="$t('core.backup.empty.message')"
      :title="$t('core.backup.empty.title')"
    >
      <template #actions>
        <VButton :loading="isFetching" @click="refetch()">
          {{ $t("core.common.buttons.refresh") }}
        </VButton>
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
