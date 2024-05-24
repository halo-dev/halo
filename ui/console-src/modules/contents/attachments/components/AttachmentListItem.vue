<script lang="ts" setup>
import {
  Dialog,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import type { Ref } from "vue";
import { computed, inject, markRaw, ref, toRefs } from "vue";
import type { Attachment } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import prettyBytes from "pretty-bytes";
import { useFetchAttachmentPolicy } from "../composables/use-attachment-policy";
import { apiClient } from "@/utils/api-client";
import { usePermission } from "@/utils/permission";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import type { OperationItem } from "@halo-dev/console-shared";
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    attachment: Attachment;
    isSelected?: boolean;
  }>(),
  { isSelected: false }
);

const { attachment } = toRefs(props);

const { policies } = useFetchAttachmentPolicy();

const emit = defineEmits<{
  (event: "select", attachment?: Attachment): void;
  (event: "open-detail", attachment: Attachment): void;
}>();

const selectedAttachments = inject<Ref<Set<Attachment>>>(
  "selectedAttachments",
  ref<Set<Attachment>>(new Set())
);

const policyDisplayName = computed(() => {
  const policy = policies.value?.find(
    (p) => p.metadata.name === props.attachment.spec.policyName
  );
  return policy?.spec.displayName;
});

const handleDelete = () => {
  Dialog.warning({
    title: t("core.attachment.operations.delete.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await apiClient.extension.storage.attachment.deleteStorageHaloRunV1alpha1Attachment(
          {
            name: props.attachment.metadata.name,
          }
        );

        selectedAttachments.value.delete(props.attachment);

        Toast.success(t("core.common.toast.delete_success"));
      } catch (e) {
        console.error("Failed to delete attachment", e);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["attachments"] });
      }
    },
  });
};

const { operationItems } = useOperationItemExtensionPoint<Attachment>(
  "attachment:list-item:operation:create",
  attachment,
  computed((): OperationItem<Attachment>[] => [
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.detail"),
      permissions: [],
      action: () => {
        emit("open-detail", attachment.value);
      },
    },
    {
      priority: 20,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.download"),
      action: () => {
        const { permalink } = attachment.value.status || {};

        if (!permalink) {
          throw new Error("Attachment has no permalink");
        }

        const a = document.createElement("a");
        a.href = permalink;
        a.download = attachment.value.spec.displayName || "unknown";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      },
    },
    {
      priority: 30,
      component: markRaw(VDropdownDivider),
    },
    {
      priority: 40,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.delete"),
      permissions: ["system:attachments:manage"],
      action: () => {
        handleDelete();
      },
    },
  ])
);
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template
      v-if="currentUserHasPermission(['system:attachments:manage'])"
      #checkbox
    >
      <input
        :checked="selectedAttachments.has(attachment)"
        type="checkbox"
        @click="emit('select', attachment)"
      />
    </template>
    <template #start>
      <VEntityField>
        <template #description>
          <div class="h-10 w-10 rounded border bg-white p-1 hover:shadow-sm">
            <AttachmentFileTypeIcon
              :display-ext="false"
              :file-name="attachment.spec.displayName"
              :width="8"
              :height="8"
            />
          </div>
        </template>
      </VEntityField>
      <VEntityField
        :title="attachment.spec.displayName"
        @click="emit('open-detail', attachment)"
      >
        <template #description>
          <VSpace>
            <span class="text-xs text-gray-500">
              {{ attachment.spec.mediaType }}
            </span>
            <span class="text-xs text-gray-500">
              {{ prettyBytes(attachment.spec.size || 0) }}
            </span>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField :description="policyDisplayName" />
      <VEntityField>
        <template #description>
          <RouterLink
            :to="{
              name: 'UserDetail',
              params: {
                name: attachment.spec.ownerName,
              },
            }"
            class="text-xs text-gray-500"
            :class="{
              'pointer-events-none': !currentUserHasPermission([
                'system:users:view',
              ]),
            }"
          >
            {{ attachment.spec.ownerName }}
          </RouterLink>
        </template>
      </VEntityField>
      <VEntityField v-if="attachment.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(attachment.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <EntityDropdownItems
        :dropdown-items="operationItems"
        :item="attachment"
      />
    </template>
  </VEntity>
</template>
