<script lang="ts" setup>
import {
  Dialog,
  Toast,
  VDropdownItem,
  VEntity,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import type { Backup } from "@halo-dev/api-client";
import { relativeTimeTo, formatDatetime } from "@/utils/date";
import { computed, markRaw } from "vue";
import { apiClient } from "@/utils/api-client";
import { useQueryClient } from "@tanstack/vue-query";
import prettyBytes from "pretty-bytes";
import { useI18n } from "vue-i18n";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import { toRefs } from "vue";
import type { OperationItem } from "@halo-dev/console-shared";

const queryClient = useQueryClient();
const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    backup: Backup;
    showOperations?: boolean;
  }>(),
  {
    showOperations: true,
  }
);

const { backup } = toRefs(props);

type Phase = {
  text: string;
  state: "default" | "warning" | "success" | "error";
  animate: boolean;
  value: "PENDING" | "RUNNING" | "SUCCEEDED" | "FAILED";
};

const phases: Phase[] = [
  {
    text: t("core.backup.list.phases.pending"),
    state: "default",
    animate: false,
    value: "PENDING",
  },
  {
    text: t("core.backup.list.phases.running"),
    state: "warning",
    animate: true,
    value: "RUNNING",
  },
  {
    text: t("core.backup.list.phases.succeeded"),
    state: "success",
    animate: false,
    value: "SUCCEEDED",
  },
  {
    text: t("core.backup.list.phases.failed"),
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

const getFailureMessage = computed(() => {
  const { phase, failureMessage } = props.backup.status || {};
  return phase === "FAILED" ? failureMessage : undefined;
});

function handleDownload() {
  window.open(
    `/apis/api.console.migration.halo.run/v1alpha1/backups/${props.backup.metadata.name}/files/${props.backup.status?.filename}`,
    "_blank"
  );
}

function handleDelete() {
  Dialog.warning({
    title: t("core.backup.operations.delete.title"),
    description: t("core.backup.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await apiClient.extension.backup.deleteMigrationHaloRunV1alpha1Backup({
        name: props.backup.metadata.name,
      });

      queryClient.invalidateQueries({ queryKey: ["backups"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
}

const { operationItems } = useOperationItemExtensionPoint<Backup>(
  "backup:list-item:operation:create",
  backup,
  computed((): OperationItem<Backup>[] => [
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.download"),
      hidden: props.backup.status?.phase !== "SUCCEEDED",
      permissions: [],
      action: () => handleDownload(),
    },
    {
      priority: 20,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.delete"),
      action: () => handleDelete(),
    },
  ])
);
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField
        :title="backup.metadata.name"
        :description="backup.status?.filename"
      >
        <template v-if="backup.status?.filename" #description>
          <VSpace class="flex-wrap">
            <span class="text-xs text-gray-500">
              {{ backup.status?.filename }}
            </span>
            <span class="text-xs text-gray-500">
              {{ prettyBytes(backup.status?.size || 0) }}
            </span>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="getPhase">
        <template #description>
          <VStatusDot
            v-tooltip="{ content: getFailureMessage }"
            :state="getPhase.state"
            :text="getPhase.text"
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
      <VEntityField
        v-if="backup.spec?.expiresAt && backup.status?.phase === 'SUCCEEDED'"
      >
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{
              $t("core.backup.list.fields.expiresAt", {
                expiresAt: relativeTimeTo(backup.spec?.expiresAt),
              })
            }}
          </span>
        </template>
      </VEntityField>
      <VEntityField v-if="backup.metadata.creationTimestamp">
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(backup.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
      <slot name="end"></slot>
    </template>
    <template v-if="showOperations" #dropdownItems>
      <EntityDropdownItems :dropdown-items="operationItems" :item="backup" />
    </template>
  </VEntity>
</template>
