<script setup lang="ts">
import type { ListedSnapshotDto } from "@halo-dev/api-client";
import { Dialog, Toast, VButton, VStatusDot, VTag } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { computed, toRefs } from "vue";
import { useI18n } from "vue-i18n";
import { SNAPSHOTS_QUERY_KEY } from "./query-keys";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    snapshot: ListedSnapshotDto;
    selectedSnapshotNames?: string[];
    cacheKey: string;
    name: string;
    releaseSnapshot?: string;
    baseSnapshot?: string;
    headSnapshot?: string;
    deleteApi: (snapshotName: string) => Promise<void>;
    revertApi: (snapshotName: string) => Promise<void>;
  }>(),
  {
    selectedSnapshotNames: undefined,
    releaseSnapshot: undefined,
    baseSnapshot: undefined,
    headSnapshot: undefined,
  }
);

const { cacheKey, name } = toRefs(props);

async function handleRestore() {
  Dialog.warning({
    title: t("core.snapshots.operations.revert.title"),
    description: t("core.snapshots.operations.revert.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await props.revertApi(props.snapshot.metadata.name);
      await queryClient.invalidateQueries({
        queryKey: SNAPSHOTS_QUERY_KEY(cacheKey, name),
      });
      Toast.success(t("core.snapshots.operations.revert.toast_success"));
    },
  });
}

function handleDelete() {
  Dialog.warning({
    title: t("core.snapshots.operations.delete.title"),
    description: t("core.snapshots.operations.delete.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    async onConfirm() {
      await props.deleteApi(props.snapshot.metadata.name);
      await queryClient.invalidateQueries({
        queryKey: SNAPSHOTS_QUERY_KEY(cacheKey, name),
      });
      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

const isSelected = computed(() => {
  return props.selectedSnapshotNames?.includes(props.snapshot.metadata.name);
});

const isReleased = computed(() => {
  return props.releaseSnapshot === props.snapshot.metadata.name;
});

const isHead = computed(() => {
  return (
    props.headSnapshot !== props.releaseSnapshot &&
    props.headSnapshot === props.snapshot.metadata.name
  );
});

const isBase = computed(() => {
  return props.baseSnapshot === props.snapshot.metadata.name;
});
</script>
<template>
  <div
    class="group relative flex cursor-pointer flex-col gap-5 p-4"
    :class="{ 'bg-gray-50': isSelected }"
  >
    <div
      v-if="isSelected"
      class="absolute inset-y-0 left-0 w-0.5 bg-primary"
    ></div>
    <div class="flex items-center justify-between">
      <div
        class="truncate text-sm"
        :class="{
          'font-semibold': isSelected,
        }"
      >
        {{ utils.date.timeAgo(snapshot.metadata.creationTimestamp) }}
      </div>
      <div class="inline-flex flex-none items-center space-x-3">
        <VTag v-if="isReleased" theme="primary">
          {{ $t("core.snapshots.status.released") }}
        </VTag>
        <VTag v-if="isHead">
          {{ $t("core.snapshots.status.draft") }}
        </VTag>
        <VTag v-if="isBase">
          {{ $t("core.snapshots.status.base") }}
        </VTag>
        <VStatusDot
          v-if="snapshot.metadata.deletionTimestamp"
          v-tooltip="$t('core.common.status.deleting')"
          state="warning"
          animate
        />
      </div>
    </div>
    <div class="flex h-6 items-end justify-between gap-2">
      <div class="flex-1 truncate text-xs text-gray-600">
        {{ snapshot.spec.owner }}
      </div>
      <div
        v-if="!isReleased"
        class="hidden flex-none space-x-2 group-hover:block"
      >
        <VButton v-if="!isHead" size="xs" @click="handleRestore()">
          {{ $t("core.snapshots.operations.revert.button") }}
        </VButton>
        <VButton v-if="!isBase" size="xs" type="danger" @click="handleDelete">
          {{ $t("core.common.buttons.delete") }}
        </VButton>
      </div>
    </div>
  </div>
</template>
