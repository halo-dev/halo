<script lang="ts" setup>
import {
  VButton,
  VEmpty,
  VEntityContainer,
  VLoading,
} from "@halo-dev/components";
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
    <VEntityContainer>
      <BackupListItem
        v-for="backup in backups?.items"
        :key="backup.metadata.name"
        :backup="backup"
      />
    </VEntityContainer>
  </Transition>
</template>
