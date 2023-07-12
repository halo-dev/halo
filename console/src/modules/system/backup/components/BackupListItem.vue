<script lang="ts" setup>
import {
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
} from "@halo-dev/components";
import type { Backup } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { computed } from "vue";

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
      <VDropdownItem type="danger"> 删除 </VDropdownItem>
    </template>
  </VEntity>
</template>
