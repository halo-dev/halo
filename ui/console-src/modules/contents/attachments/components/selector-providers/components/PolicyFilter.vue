<script lang="ts" setup>
import { attachmentPolicyLabels } from "@/constants/labels";
import type { PolicyTemplate } from "@halo-dev/api-client";
import { IconAddCircle, VDropdown, VDropdownItem } from "@halo-dev/components";
import { computed, ref } from "vue";
import {
  useFetchAttachmentPolicy,
  useFetchAttachmentPolicyTemplate,
} from "../../../composables/use-attachment-policy";
import AttachmentPolicyBadge from "../../AttachmentPolicyBadge.vue";
import AttachmentPolicyEditingModal from "../../AttachmentPolicyEditingModal.vue";

const modelValue = defineModel<string | undefined>();

const { data: policyTemplates } = useFetchAttachmentPolicyTemplate();
const { data: allPolicies, refetch: handleFetchPolicies } =
  useFetchAttachmentPolicy();

const policies = computed(() => {
  return allPolicies.value?.filter((policy) => {
    return policy.metadata.labels?.[attachmentPolicyLabels.HIDDEN] !== "true";
  });
});

const policyCreationModalVisible = ref(false);
const policyTemplateNameToCreate = ref<string | undefined>();

async function onEditingModalClose() {
  await handleFetchPolicies();
  policyTemplateNameToCreate.value = undefined;
  modelValue.value = policies.value?.[0].metadata.name;
  policyCreationModalVisible.value = false;
}

function handleOpenCreationModal(policyTemplate: PolicyTemplate) {
  policyTemplateNameToCreate.value = policyTemplate.metadata.name;
  policyCreationModalVisible.value = true;
}
</script>
<template>
  <div class="flex flex-col gap-2">
    <div class="text-sm text-gray-800">
      {{ $t("core.attachment.upload_modal.filters.policy.label") }}
    </div>
    <div class="grid grid-cols-3 gap-x-2 gap-y-3 sm:grid-cols-5">
      <AttachmentPolicyBadge
        :features="{ checkIcon: true }"
        :is-selected="!modelValue"
        @click="modelValue = undefined"
      >
        <template #text>
          <span>{{ $t("core.common.filters.item_labels.all") }}</span>
        </template>
      </AttachmentPolicyBadge>
      <AttachmentPolicyBadge
        v-for="policy in policies"
        :key="policy.metadata.name"
        :policy="policy"
        :is-selected="modelValue === policy.metadata.name"
        :features="{ checkIcon: true }"
        @click="modelValue = policy.metadata.name"
      />

      <HasPermission :permissions="['system:attachments:manage']">
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
              v-for="policyTemplate in policyTemplates"
              :key="policyTemplate.metadata.name"
              @click="handleOpenCreationModal(policyTemplate)"
            >
              {{ policyTemplate.spec?.displayName }}
            </VDropdownItem>
          </template>
        </VDropdown>
      </HasPermission>
    </div>
  </div>

  <AttachmentPolicyEditingModal
    v-if="policyCreationModalVisible"
    :template-name="policyTemplateNameToCreate"
    @close="onEditingModalClose"
  />
</template>
