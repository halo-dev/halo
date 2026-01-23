<script lang="ts" setup>
import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import {
  IconAddCircle,
  VButton,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VEntityContainer,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { ref } from "vue";
import {
  useFetchAttachmentPolicy,
  useFetchAttachmentPolicyTemplate,
} from "../composables/use-attachment-policy";
import AttachmentPoliciesListItem from "./AttachmentPoliciesListItem.vue";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";

const emit = defineEmits<{
  (event: "close"): void;
}>();

const {
  data: policies,
  isLoading,
  refetch: handleFetchPolicies,
} = useFetchAttachmentPolicy();
const { data: policyTemplates } = useFetchAttachmentPolicyTemplate();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const selectedPolicy = ref<Policy>();
const selectedTemplateName = ref();

const creationModalVisible = ref(false);

const handleOpenCreateNewPolicyModal = (policyTemplate: PolicyTemplate) => {
  selectedTemplateName.value = policyTemplate.metadata.name;
  creationModalVisible.value = true;
};

const onCreationModalClose = () => {
  selectedPolicy.value = undefined;
  selectedTemplateName.value = undefined;
  creationModalVisible.value = false;
};
</script>
<template>
  <VModal
    ref="modal"
    :width="750"
    :title="$t('core.attachment.policies_modal.title')"
    :body-class="['!p-0']"
    :layer-closable="true"
    :centered="false"
    @close="emit('close')"
  >
    <template #actions>
      <VDropdown>
        <span v-tooltip="$t('core.common.buttons.new')">
          <IconAddCircle />
        </span>
        <template #popper>
          <VDropdownItem
            v-for="policyTemplate in policyTemplates"
            :key="policyTemplate.metadata.name"
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
                <IconAddCircle />
              </template>
              {{ $t("core.common.buttons.new") }}
            </VButton>
            <template #popper>
              <VDropdownItem
                v-for="policyTemplate in policyTemplates"
                :key="policyTemplate.metadata.name"
                @click="handleOpenCreateNewPolicyModal(policyTemplate)"
              >
                {{ policyTemplate.spec?.displayName }}
              </VDropdownItem>
            </template>
          </VDropdown>
        </VSpace>
      </template>
    </VEmpty>
    <VEntityContainer v-else>
      <AttachmentPoliciesListItem
        v-for="policy in policies"
        :key="policy.metadata.name"
        :policy="policy"
      />
    </VEntityContainer>
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close_and_shortcut") }}
      </VButton>
    </template>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="creationModalVisible"
    :policy="selectedPolicy"
    :template-name="selectedTemplateName"
    @close="onCreationModalClose"
  />
</template>
