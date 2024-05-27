<script lang="ts" setup>
import type { PolicyTemplate } from "@halo-dev/api-client";
import {
  IconAddCircle,
  VAlert,
  VDropdown,
  VDropdownItem,
  VModal,
} from "@halo-dev/components";
import { useLocalStorage } from "@vueuse/core";
import { ref, watch } from "vue";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import {
  useFetchAttachmentPolicy,
  useFetchAttachmentPolicyTemplate,
} from "../composables/use-attachment-policy";
import AttachmentGroupBadge from "./AttachmentGroupBadge.vue";
import AttachmentPolicyBadge from "./AttachmentPolicyBadge.vue";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { groups } = useFetchAttachmentGroup();
const { policies, handleFetchPolicies } = useFetchAttachmentPolicy();
const { policyTemplates } = useFetchAttachmentPolicyTemplate();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const selectedGroupName = useLocalStorage("attachment-upload-group", "");
const selectedPolicyName = useLocalStorage("attachment-upload-policy", "");
const policyEditingModal = ref(false);
const policyTemplateNameToCreate = ref();

watch(
  () => groups.value,
  () => {
    if (selectedGroupName.value === "") return;

    const group = groups.value?.find(
      (group) => group.metadata.name === selectedGroupName.value
    );
    if (!group) {
      selectedGroupName.value = groups.value?.length
        ? groups.value[0].metadata.name
        : "";
    }
  }
);

watch(
  () => policies.value,
  () => {
    const policy = policies.value?.find(
      (policy) => policy.metadata.name === selectedPolicyName.value
    );
    if (!policy) {
      selectedPolicyName.value = policies.value?.length
        ? policies.value[0].metadata.name
        : "";
    }
  }
);

const handleOpenCreateNewPolicyModal = (policyTemplate: PolicyTemplate) => {
  policyTemplateNameToCreate.value = policyTemplate.metadata.name;
  policyEditingModal.value = true;
};

const onEditingModalClose = async () => {
  await handleFetchPolicies();
  policyTemplateNameToCreate.value = undefined;
  selectedPolicyName.value = policies.value?.[0].metadata.name;
  policyEditingModal.value = false;
};
</script>
<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :width="920"
    height="calc(100vh - 20px)"
    :title="$t('core.attachment.upload_modal.title')"
    mount-to-body
    @close="emit('close')"
  >
    <div class="w-full p-4">
      <div class="mb-2">
        <span class="text-sm text-gray-800">
          {{ $t("core.attachment.upload_modal.filters.policy.label") }}
        </span>
      </div>
      <div class="mb-3 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-4">
        <AttachmentPolicyBadge
          v-for="policy in policies"
          :key="policy.metadata.name"
          :policy="policy"
          :is-selected="selectedPolicyName === policy.metadata.name"
          :features="{ checkIcon: true }"
          @click="selectedPolicyName = policy.metadata.name"
        />

        <VDropdown>
          <AttachmentPolicyBadge>
            <template #text>
              <span>{{ $t("core.common.buttons.new") }}</span>
            </template>
            <template #actions>
              <IconAddCircle />
            </template>
          </AttachmentPolicyBadge>
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
      </div>
      <div v-if="!policies?.length" class="mb-3">
        <VAlert
          :title="$t('core.attachment.upload_modal.filters.policy.empty.title')"
          :description="
            $t('core.attachment.upload_modal.filters.policy.empty.description')
          "
          :closable="false"
        />
      </div>
      <div class="mb-2">
        <span class="text-sm text-gray-800">
          {{ $t("core.attachment.upload_modal.filters.group.label") }}
        </span>
      </div>
      <div class="mb-3 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-4">
        <AttachmentGroupBadge
          v-for="group in [
            {
              metadata: { name: '' },
              apiVersion: '',
              kind: '',
              spec: {
                displayName: $t('core.attachment.common.text.ungrouped'),
              },
            },
            ...(groups || []),
          ]"
          :key="group.metadata.name"
          :group="group"
          :is-selected="group.metadata.name === selectedGroupName"
          :features="{ actions: false, checkIcon: true }"
          @click="selectedGroupName = group.metadata.name"
        >
        </AttachmentGroupBadge>
      </div>
      <UppyUpload
        endpoint="/apis/api.console.halo.run/v1alpha1/attachments/upload"
        :disabled="!selectedPolicyName"
        :meta="{
          policyName: selectedPolicyName,
          groupName: selectedGroupName,
        }"
        width="100%"
        :allowed-meta-fields="['policyName', 'groupName']"
        :note="
          selectedPolicyName
            ? ''
            : $t('core.attachment.upload_modal.filters.policy.not_select')
        "
        :done-button-handler="() => modal?.close()"
      />
    </div>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="policyEditingModal"
    :template-name="policyTemplateNameToCreate"
    @close="onEditingModalClose"
  />
</template>
