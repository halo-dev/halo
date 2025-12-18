<script lang="ts" setup>
import { SYSTEM_PROTECTION } from "@/constants/finalizers";
import { attachmentPolicyLabels } from "@/constants/labels";
import {
  consoleApiClient,
  coreApiClient,
  type Policy,
} from "@halo-dev/api-client";
import {
  Dialog,
  IconEyeOff,
  Toast,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useFetchAttachmentPolicyTemplate } from "../composables/use-attachment-policy";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";

const queryClient = useQueryClient();

const props = defineProps<{
  policy: Policy;
}>();

const { t } = useI18n();

const { data: policyTemplates } = useFetchAttachmentPolicyTemplate();

const templateDisplayName = computed(() => {
  const policyTemplate = policyTemplates.value?.find(
    (template) => template.metadata.name === props.policy.spec.templateName
  );
  return policyTemplate?.spec?.displayName || "--";
});

const editingModalVisible = ref(false);

const handleDelete = async () => {
  const { data } = await consoleApiClient.storage.attachment.searchAttachments({
    fieldSelector: [`spec.policyName=${props.policy.metadata.name}`],
  });

  if (data.total > 0) {
    Dialog.warning({
      title: t(
        "core.attachment.policies_modal.operations.can_not_delete.title"
      ),
      description: t(
        "core.attachment.policies_modal.operations.can_not_delete.description"
      ),
      confirmText: t("core.common.buttons.confirm"),
      showCancel: false,
    });
    return;
  }

  Dialog.warning({
    title: t("core.attachment.policies_modal.operations.delete.title"),
    description: t(
      "core.attachment.policies_modal.operations.delete.description"
    ),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await coreApiClient.storage.policy.deletePolicy({
        name: props.policy.metadata.name,
      });

      Toast.success(t("core.common.toast.delete_success"));
      queryClient.invalidateQueries({ queryKey: ["attachment-policies"] });
    },
  });
};

const isHidden = computed(() => {
  return (
    props.policy.metadata.labels?.[attachmentPolicyLabels.HIDDEN] === "true"
  );
});

const isSystemProtection = computed(() => {
  return props.policy.metadata.finalizers?.includes(SYSTEM_PROTECTION);
});
</script>
<template>
  <VEntity :key="policy.metadata.name">
    <template #start>
      <VEntityField
        :title="policy.spec.displayName"
        :description="templateDisplayName"
      ></VEntityField>
    </template>
    <template #end>
      <VEntityField>
        <template v-if="isSystemProtection" #description>
          <VTag>{{ $t("core.common.text.system_protection") }}</VTag>
        </template>
      </VEntityField>
      <VEntityField v-if="isHidden">
        <template #description>
          <IconEyeOff class="text-sm transition-all hover:text-blue-600" />
        </template>
      </VEntityField>
      <VEntityField v-if="policy.metadata.deletionTimestamp">
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
            {{ utils.date.format(policy.metadata.creationTimestamp) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem
        :disabled="isSystemProtection"
        @click="editingModalVisible = true"
      >
        {{ $t("core.common.buttons.edit") }}
      </VDropdownItem>
      <VDropdownItem
        :disabled="isSystemProtection"
        type="danger"
        @click="handleDelete"
      >
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>
  </VEntity>

  <AttachmentPolicyEditingModal
    v-if="editingModalVisible"
    :policy="policy"
    @close="editingModalVisible = false"
  />
</template>
