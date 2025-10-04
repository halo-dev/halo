<script lang="ts" setup>
import { attachmentPolicyLabels } from "@/constants/labels";
import type { PolicyTemplate } from "@halo-dev/api-client";
import {
  IconAddCircle,
  VAlert,
  VDropdown,
  VDropdownItem,
  VModal,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import {
  useFetchAttachmentPolicy,
  useFetchAttachmentPolicyTemplate,
} from "../composables/use-attachment-policy";
import AttachmentGroupBadge from "./AttachmentGroupBadge.vue";
import AttachmentGroupEditingModal from "./AttachmentGroupEditingModal.vue";
import AttachmentPolicyBadge from "./AttachmentPolicyBadge.vue";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";
import UploadFromUrl from "./UploadFromUrl.vue";

const { initialPolicyName, initialGroupName } = defineProps<{
  initialPolicyName?: string;
  initialGroupName?: string;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const selectedGroupName = ref(initialGroupName || "");
const selectedPolicyName = ref(initialPolicyName);
const policyEditingModal = ref(false);
const groupEditingModal = ref(false);
const policyTemplateNameToCreate = ref();

const { groups, handleFetchGroups } = useFetchAttachmentGroup();
const { policyTemplates } = useFetchAttachmentPolicyTemplate();
const { policies: allPolicies, handleFetchPolicies } =
  useFetchAttachmentPolicy();

const policies = computed(() => {
  return allPolicies.value?.filter((policy) => {
    return policy.metadata.labels?.[attachmentPolicyLabels.HIDDEN] !== "true";
  });
});

onMounted(() => {
  const initialPolicy = policies.value?.find(
    (p) => p.metadata.name === initialPolicyName
  );
  selectedPolicyName.value =
    initialPolicy?.metadata.name || policies.value?.[0]?.metadata.name;
});

const handleOpenCreateNewPolicyModal = async (
  policyTemplate: PolicyTemplate
) => {
  policyTemplateNameToCreate.value = policyTemplate.metadata.name;
  policyEditingModal.value = true;
};

const handleOpenCreateNewGroupModal = () => {
  groupEditingModal.value = true;
};

const onEditingModalClose = async () => {
  await handleFetchPolicies();
  policyTemplateNameToCreate.value = undefined;
  selectedPolicyName.value = policies.value?.[0].metadata.name;
  policyEditingModal.value = false;
};

const onGroupEditingModalClose = async () => {
  await handleFetchGroups();
  groupEditingModal.value = false;
};

const activeTab = ref("upload");
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
        />

        <AttachmentGroupBadge
          :features="{ actions: false }"
          @click="handleOpenCreateNewGroupModal"
        >
          <template #text>
            <span>{{ $t("core.common.buttons.new") }}</span>
          </template>
          <template #actions>
            <IconAddCircle />
          </template>
        </AttachmentGroupBadge>
      </div>

      <div class="mb-3">
        <VTabs v-model:active-id="activeTab" type="outline">
          <VTabItem
            id="upload"
            :label="
              $t('core.attachment.upload_modal.upload_options.local_upload')
            "
          >
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
          </VTabItem>
          <VTabItem
            id="download"
            :label="$t('core.attachment.upload_modal.upload_options.download')"
          >
            <UploadFromUrl
              :policy-name="selectedPolicyName"
              :group-name="selectedGroupName"
            />
          </VTabItem>
        </VTabs>
      </div>
    </div>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="policyEditingModal"
    :template-name="policyTemplateNameToCreate"
    @close="onEditingModalClose"
  />

  <AttachmentGroupEditingModal
    v-if="groupEditingModal"
    @close="onGroupEditingModalClose"
  />
</template>
