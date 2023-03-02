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
      title: "删除失败",
      description: "该策略下存在附件，无法删除。",
    });
    return;
  }

  Dialog.warning({
    title: "确定要删除该策略吗？",
    description: "当前策略下没有已上传的附件。",
    onConfirm: async () => {
      await apiClient.extension.storage.policy.deletestorageHaloRunV1alpha1Policy(
        { name: policy.metadata.name }
      );

      Toast.success("删除成功");
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
    title="存储策略"
    :body-class="['!p-0']"
    :layer-closable="true"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <FloatingDropdown>
        <span v-tooltip="`添加存储策略`">
          <IconAddCircle />
        </span>
        <template #popper>
          <div class="w-72 p-4">
            <ul class="space-y-1">
              <li
                v-for="(policyTemplate, index) in policyTemplates"
                :key="index"
                v-close-popper
                class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                @click="handleOpenCreateNewPolicyModal(policyTemplate)"
              >
                <span class="truncate">
                  {{ policyTemplate.spec?.displayName }}
                </span>
              </li>
            </ul>
          </div>
        </template>
      </FloatingDropdown>
    </template>
    <VEmpty
      v-if="!policies?.length && !isLoading"
      message="当前没有可用的存储策略，你可以尝试刷新或者新建策略"
      title="当前没有可用的存储策略"
    >
      <template #actions>
        <VSpace>
          <VButton @click="handleFetchPolicies">刷新</VButton>
          <FloatingDropdown>
            <VButton type="secondary">
              <template #icon>
                <IconAddCircle class="h-full w-full" />
              </template>
              新建策略
            </VButton>
            <template #popper>
              <div class="w-72 p-4">
                <ul class="space-y-1">
                  <li
                    v-for="(policyTemplate, index) in policyTemplates"
                    :key="index"
                    v-close-popper
                    class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                    @click="handleOpenCreateNewPolicyModal(policyTemplate)"
                  >
                    <span class="truncate">
                      {{ policyTemplate.spec?.displayName }}
                    </span>
                  </li>
                </ul>
              </div>
            </template>
          </FloatingDropdown>
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
                <VStatusDot v-tooltip="`删除中`" state="warning" animate />
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
            <VButton
              v-close-popper
              block
              type="secondary"
              @click="handleOpenEditingModal(policy)"
            >
              编辑
            </VButton>
            <VButton
              v-close-popper
              block
              type="danger"
              @click="handleDelete(policy)"
            >
              删除
            </VButton>
          </template>
        </VEntity>
      </li>
    </ul>
    <template #footer>
      <VButton @click="onVisibleChange(false)">关闭 Esc</VButton>
    </template>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="visible"
    v-model:visible="policyEditingModal"
    :policy="selectedPolicy"
    @close="onEditingModalClose"
  />
</template>
