<script lang="ts" setup>
import {
  IconAddCircle,
  VAlert,
  VDropdown,
  VDropdownItem,
  VModal,
} from "@halo-dev/components";
import { ref, watch } from "vue";
import type { PolicyTemplate } from "@halo-dev/api-client";
import {
  useFetchAttachmentPolicy,
  useFetchAttachmentPolicyTemplate,
} from "../composables/use-attachment-policy";
import { useFetchAttachmentGroup } from "../composables/use-attachment-group";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";
import { useLocalStorage } from "@vueuse/core";

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { groups } = useFetchAttachmentGroup();
const { policies, handleFetchPolicies } = useFetchAttachmentPolicy();
const { policyTemplates } = useFetchAttachmentPolicyTemplate();

const modal = ref();
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
  selectedPolicyName.value = policies.value?.[0].metadata.name;
  policyEditingModal.value = false;
};
</script>
<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :width="650"
    :centered="false"
    :title="$t('core.attachment.upload_modal.title')"
    @close="emit('close')"
  >
    <div class="w-full p-4">
      <div class="mb-2">
        <span class="text-sm text-gray-800">
          {{ $t("core.attachment.upload_modal.filters.group.label") }}
        </span>
      </div>
      <div class="mb-3 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-4">
        <div
          v-for="(group, index) in [
            {
              metadata: { name: '' },
              spec: {
                displayName: $t('core.attachment.common.text.ungrouped'),
              },
            },
            ...(groups || []),
          ]"
          :key="index"
          :class="{
            '!bg-gray-200 !text-gray-900':
              group.metadata.name === selectedGroupName,
          }"
          class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
          @click="selectedGroupName = group.metadata.name"
        >
          <div class="flex flex-1 items-center gap-2 truncate">
            <span class="truncate text-sm">
              {{ group.spec.displayName }}
            </span>
          </div>
        </div>
      </div>
      <div class="mb-2">
        <span class="text-sm text-gray-800">
          {{ $t("core.attachment.upload_modal.filters.policy.label") }}
        </span>
      </div>
      <div class="mb-3 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-4">
        <div
          v-for="(policy, index) in policies"
          :key="index"
          :class="{
            '!bg-gray-200 !text-gray-900':
              selectedPolicyName === policy.metadata.name,
          }"
          class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
          @click="selectedPolicyName = policy.metadata.name"
        >
          <div class="flex flex-1 flex-col items-start truncate">
            <span class="truncate text-sm">
              {{ policy.spec.displayName }}
            </span>
            <span class="text-xs">
              {{ policy.spec.templateName }}
            </span>
          </div>
        </div>

        <VDropdown>
          <div
            class="flex h-full cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
          >
            <div class="flex flex-1 items-center truncate">
              <span class="truncate text-sm">
                {{ $t("core.common.buttons.new") }}
              </span>
            </div>
            <IconAddCircle />
          </div>
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
      <UppyUpload
        endpoint="/apis/api.console.halo.run/v1alpha1/attachments/upload"
        :disabled="!selectedPolicyName"
        :meta="{
          policyName: selectedPolicyName,
          groupName: selectedGroupName,
        }"
        :allowed-meta-fields="['policyName', 'groupName']"
        :note="
          selectedPolicyName
            ? ''
            : $t('core.attachment.upload_modal.filters.policy.not_select')
        "
        :done-button-handler="() => modal.close()"
      />
    </div>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="policyEditingModal"
    :template-name="policyTemplateNameToCreate"
    @close="onEditingModalClose"
  />
</template>
