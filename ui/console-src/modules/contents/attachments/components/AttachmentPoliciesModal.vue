<script lang="ts" setup>
import {
  Dialog,
  IconAddCircle,
  Toast,
  VButton,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VEntity,
  VEntityField,
  VModal,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";
import { ref } from "vue";
import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import {
  useFetchAttachmentPolicy,
  useFetchAttachmentPolicyTemplate,
} from "../composables/use-attachment-policy";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { t } = useI18n();

const { policies, isLoading, handleFetchPolicies } = useFetchAttachmentPolicy();
const { policyTemplates } = useFetchAttachmentPolicyTemplate();

const modal = ref();
const selectedPolicy = ref<Policy>();
const selectedTemplateName = ref();

const policyEditingModal = ref(false);

const handleOpenEditingModal = (policy: Policy) => {
  selectedPolicy.value = policy;
  policyEditingModal.value = true;
};

const handleOpenCreateNewPolicyModal = (policyTemplate: PolicyTemplate) => {
  selectedTemplateName.value = policyTemplate.metadata.name;
  policyEditingModal.value = true;
};

const handleDelete = async (policy: Policy) => {
  const { data } = await apiClient.attachment.searchAttachments({
    fieldSelector: [`spec.policyName=${policy.metadata.name}`],
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
      cancelText: t("core.common.buttons.cancel"),
    });
    return;
  }

  Dialog.warning({
    title: t("core.attachment.policies_modal.operations.delete.title"),
    description: t(
      "core.attachment.policies_modal.operations.delete.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await apiClient.extension.storage.policy.deleteStorageHaloRunV1alpha1Policy(
        { name: policy.metadata.name }
      );

      Toast.success(t("core.common.toast.delete_success"));
      handleFetchPolicies();
    },
  });
};

const onEditingModalClose = () => {
  selectedPolicy.value = undefined;
  handleFetchPolicies();
  policyEditingModal.value = false;
};
</script>
<template>
  <VModal
    ref="modal"
    :width="750"
    :title="$t('core.attachment.policies_modal.title')"
    :body-class="['!p-0']"
    :layer-closable="true"
    @close="emit('close')"
  >
    <template #actions>
      <VDropdown>
        <span v-tooltip="$t('core.common.buttons.new')">
          <IconAddCircle />
        </span>
        <template #popper>
          <VDropdownItem
            v-for="(policyTemplate, index) in policyTemplates"
            :key="index"
            @click="handleOpenCreateNewPolicyModal(policyTemplate)"
          >
            {{ policyTemplate.spec?.displayName }}
          </VDropdownItem>
        </template>
      </VDropdown>
    </template>
    <VEmpty
      v-if="!policies?.length && !isLoading"
      :message="$t('core.attachment.policies_modal.empty.message')"
      :title="$t('core.attachment.policies_modal.empty.title')"
    >
      <template #actions>
        <VSpace>
          <VButton @click="handleFetchPolicies">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
          <VDropdown>
            <VButton type="secondary">
              <template #icon>
                <IconAddCircle class="h-full w-full" />
              </template>
              {{ $t("core.common.buttons.new") }}
            </VButton>
            <template #popper>
              <VDropdownItem
                v-for="(policyTemplate, index) in policyTemplates"
                :key="index"
                @click="handleOpenCreateNewPolicyModal(policyTemplate)"
              >
                {{ policyTemplate.spec?.displayName }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </VSpace>
      </template>
    </VEmpty>
    <ul
      v-else
      class="box-border h-full w-full divide-y divide-gray-100"
      role="list"
    >
      <li v-for="(policy, index) in policies" :key="index">
        <VEntity>
          <template #start>
            <VEntityField
              :title="policy.spec.displayName"
              :description="policy.spec.templateName"
            ></VEntityField>
          </template>
          <template #end>
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
                  {{ formatDatetime(policy.metadata.creationTimestamp) }}
                </span>
              </template>
            </VEntityField>
          </template>
          <template #dropdownItems>
            <VDropdownItem @click="handleOpenEditingModal(policy)">
              {{ $t("core.common.buttons.edit") }}
            </VDropdownItem>
            <VDropdownItem type="danger" @click="handleDelete(policy)">
              {{ $t("core.common.buttons.delete") }}
            </VDropdownItem>
          </template>
        </VEntity>
      </li>
    </ul>
    <template #footer>
      <VButton @click="modal.close()">
        {{ $t("core.common.buttons.close_and_shortcut") }}
      </VButton>
    </template>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="policyEditingModal"
    :policy="selectedPolicy"
    :template-name="selectedTemplateName"
    @close="onEditingModalClose"
  />
</template>
