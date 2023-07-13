<script lang="ts" setup>
import {
  Dialog,
  Toast,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
} from "@halo-dev/components";
import type { Backup } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { computed } from "vue";
import { apiClient } from "@/utils/api-client";
import { useQueryClient } from "@tanstack/vue-query";

const queryClient = useQueryClient();

const props = defineProps<{
  backup: Backup;
}>();

type Phase = {
  tooltip: string;
  state: "default" | "warning" | "success" | "error";
  animate: boolean;
  value: "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED";
};

const phases: Phase[] = [
  {
    tooltip: "准备中",
    state: "default",
    animate: false,
    value: "PENDING",
  },
  {
    tooltip: "备份中",
    state: "warning",
    animate: true,
    value: "RUNNING",
  },
  {
    tooltip: "备份完成",
    state: "success",
    animate: false,
    value: "SUCCEEDED",
  },
  {
    tooltip: "备份失败",
    state: "error",
    animate: false,
    value: "FAILED",
  },
];

const getPhase = computed(() => {
  if (!props.backup.status?.phase) {
    return undefined;
  }
  return phases.find((phase) => phase.value === props.backup.status?.phase);
});

function handleDelete() {
  Dialog.warning({
    title: "删除备份",
    description: "确定要删除该备份吗？",
    async onConfirm() {
      await apiClient.extension.backup.deletemigrationHaloRunV1alpha1Backup({
        name: props.backup.metadata.name,
      });

      queryClient.invalidateQueries({ queryKey: ["backups"] });

      Toast.success("删除成功");
    },
  });
}
</script>

<template>
  <VEntity>
    <template #checkbox>
      <input
        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
        name="backup-checkbox"
        type="checkbox"
      />
    </template>
    <template #start>
      <VEntityField :title="backup.status?.filename"> </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="getPhase">
        <template #description>
          <VStatusDot
            v-tooltip="getPhase.tooltip"
            :state="getPhase.state"
            :animate="getPhase.animate"
          />
        </template>
      </VEntityField>
      <VEntityField v-if="backup.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField v-if="backup.metadata.creationTimestamp">
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(backup.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem> 下载 </VDropdownItem>
      <VDropdownItem type="danger" @click="handleDelete"> 删除 </VDropdownItem>
    </template>
  </VEntity>
</template>
