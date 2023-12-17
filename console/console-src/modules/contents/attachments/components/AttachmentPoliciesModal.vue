<script lang="ts" setup>
import {
  IconAddCircle,
  VButton,
  VModal,
  VSpace,
  VEmpty,
  Dialog,
  VEntity,
  VEntityField,
  VStatusDot,
  VDropdown,
  VDropdownItem,
  Toast,
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

withDefaults(
  defineProps<{
    visible: boolean;
  }>(),
  {
    visible: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const { t } = useI18n();

const { policies, isLoading, handleFetchPolicies } = useFetchAttachmentPolicy();
const { policyTemplates } = useFetchAttachmentPolicyTemplate();

const selectedPolicy = ref<Policy>();

const policyEditingModal = ref(false);

function onVisibleChange(visible: boolean) {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
}

const handleOpenEditingModal = (policy: Policy) => {
  selectedPolicy.value = policy;
  policyEditingModal.value = true;
};

const handleOpenCreateNewPolicyModal = (policyTemplate: PolicyTemplate) => {
  selectedPolicy.value = {
    spec: {
      displayName: "",
      templateName: policyTemplate.metadata.name,
      configMapName: "",
    },
    apiVersion: "storage.halo.run/v1alpha1",
    kind: "Policy",
    metadata: {
      name: "",
      generateName: "attachment-policy-",
    },
  };
  policyEditingModal.value = true;
};

const handleDelete = async (policy: Policy) => {
  const { data } = await apiClient.attachment.searchAttachments({
    policy: policy.metadata.name,
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
      await apiClient.extension.storage.policy.deletestorageHaloRunV1alpha1Policy(
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
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="750"
    :title="$t('core.attachment.policies_modal.title')"
    :body-class="['!p-0']"
    :layer-closable="true"
    @update:visible="onVisibleChange"
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
      <VButton @click="onVisibleChange(false)">
        {{ $t("core.common.buttons.close_and_shortcut") }}
      </VButton>
    </template>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="visible"
    v-model:visible="policyEditingModal"
    :policy="selectedPolicy"
    @close="onEditingModalClose"
  />
</template>
