<script setup lang="ts">
import type { ListedSnapshotDto, Post } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast, VButton, VStatusDot, VTag } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { computed } from "vue";
import { relativeTimeTo } from "@/utils/date";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    post?: Post;
    snapshot: ListedSnapshotDto;
    selectedSnapshotName?: string;
  }>(),
  {
    post: undefined,
    selectedSnapshotName: undefined,
  }
);

async function handleRestore() {
  Dialog.warning({
    title: t("core.post_snapshots.operations.revert.title"),
    description: t("core.post_snapshots.operations.revert.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await apiClient.post.revertToSpecifiedSnapshotForPost({
        name: props.post?.metadata.name as string,
        revertSnapshotForPostParam: {
          snapshotName: props.snapshot.metadata.name,
        },
      });
      await queryClient.invalidateQueries({
        queryKey: ["post-snapshots-by-post-name"],
      });
      Toast.success(t("core.post_snapshots.operations.revert.toast_success"));
    },
  });
}

function handleDelete() {
  Dialog.warning({
    title: t("core.post_snapshots.operations.delete.title"),
    description: t("core.post_snapshots.operations.delete.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await apiClient.post.deletePostContent({
        name: props.post?.metadata.name as string,
        snapshotName: props.snapshot.metadata.name,
      });
      await queryClient.invalidateQueries({
        queryKey: ["post-snapshots-by-post-name"],
      });
      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

const isSelected = computed(() => {
  return props.selectedSnapshotName === props.snapshot.metadata.name;
});

const isReleased = computed(() => {
  return props.post?.spec.releaseSnapshot === props.snapshot.metadata.name;
});

const isHead = computed(() => {
  const { headSnapshot, releaseSnapshot } = props.post?.spec || {};
  return (
    headSnapshot !== releaseSnapshot &&
    headSnapshot === props.snapshot.metadata.name
  );
});

const isBase = computed(() => {
  return props.post?.spec.baseSnapshot === props.snapshot.metadata.name;
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
        {{ relativeTimeTo(snapshot.metadata.creationTimestamp) }}
      </div>
      <div class="inline-flex flex-none items-center space-x-3">
        <VTag v-if="isReleased" theme="primary">
          {{ $t("core.post_snapshots.status.released") }}
        </VTag>
        <VTag v-if="isHead">
          {{ $t("core.post_snapshots.status.draft") }}
        </VTag>
        <VTag v-if="isBase">
          {{ $t("core.post_snapshots.status.base") }}
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
          {{ $t("core.post_snapshots.operations.revert.button") }}
        </VButton>
        <VButton v-if="!isBase" size="xs" type="danger" @click="handleDelete">
          {{ $t("core.common.buttons.delete") }}
        </VButton>
      </div>
    </div>
  </div>
</template>
