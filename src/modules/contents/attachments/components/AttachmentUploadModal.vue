<script lang="ts" setup>
import { VModal, IconAddCircle, VAlert } from "@halo-dev/components";
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { computed, ref, watch, watchEffect } from "vue";
import type { Policy, Group, PolicyTemplate } from "@halo-dev/api-client";
import {
  useFetchAttachmentPolicy,
  useFetchAttachmentPolicyTemplate,
} from "../composables/use-attachment-policy";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    group?: Group;
  }>(),
  {
    visible: false,
    group: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const { policies, handleFetchPolicies } = useFetchAttachmentPolicy({
  fetchOnMounted: false,
});
const { policyTemplates, handleFetchPolicyTemplates } =
  useFetchAttachmentPolicyTemplate();

const selectedPolicy = ref<Policy>();
const policyToCreate = ref<Policy>();
const uploadVisible = ref(false);
const policyEditingModal = ref(false);

const modalTitle = computed(() => {
  if (props.group && props.group.metadata.name) {
    return `上传附件到分组：${props.group.spec.displayName}`;
  }
  return "上传附件";
});

watchEffect(() => {
  if (policies.value.length) {
    selectedPolicy.value = policies.value[0];
  }
});

const handleOpenCreateNewPolicyModal = (policyTemplate: PolicyTemplate) => {
  policyToCreate.value = {
    spec: {
      displayName: "",
      templateRef: {
        name: policyTemplate.metadata.name,
      },
      configMapRef: {
        name: "",
      },
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

const onEditingModalClose = async () => {
  await handleFetchPolicies();
  policyToCreate.value = policies.value[0];
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
    policyEditingModal.value = false;
  }
};

watch(
  () => props.visible,
  (newValue) => {
    if (newValue) {
      handleFetchPolicies();
      handleFetchPolicyTemplates();
      uploadVisible.value = true;
    } else {
      const uploadVisibleTimer = setTimeout(() => {
        uploadVisible.value = false;
        clearTimeout(uploadVisibleTimer);
      }, 200);
    }
  }
);
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    :visible="visible"
    :width="650"
    :title="modalTitle"
    @update:visible="onVisibleChange"
  >
    <div class="w-full p-4">
      <div class="mb-2">
        <span class="text-sm text-gray-900">选择存储策略：</span>
      </div>
      <div class="mb-3 grid grid-cols-2 gap-x-2 gap-y-3 sm:grid-cols-4">
        <div
          v-for="(policy, index) in policies"
          :key="index"
          :class="{
            '!bg-gray-200 !text-gray-900':
              selectedPolicy?.metadata.name === policy.metadata.name,
          }"
          class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
          @click="selectedPolicy = policy"
        >
          <div class="flex flex-1 flex-col items-start truncate">
            <span class="truncate text-sm">
              {{ policy.spec.displayName }}
            </span>
            <span class="text-xs">
              {{ policy.spec.templateRef?.name }}
            </span>
          </div>
        </div>

        <FloatingDropdown>
          <div
            class="flex h-full cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
          >
            <div class="flex flex-1 items-center truncate">
              <span class="truncate text-sm">新建策略</span>
            </div>
            <IconAddCircle />
          </div>
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
      </div>
      <div v-if="policies.length <= 0" class="mb-3">
        <VAlert
          title="没有存储策略"
          description="在上传之前，需要新建一个存储策略"
          :closable="false"
        />
      </div>
      <UppyUpload
        v-if="uploadVisible"
        endpoint="/apis/api.console.halo.run/v1alpha1/attachments/upload"
        :disabled="!selectedPolicy"
        :meta="{ 
          policyName: selectedPolicy?.metadata.name as string,
          groupName: props.group?.metadata.name as string
        }"
        :allowed-meta-fields="['policyName', 'groupName']"
        :note="selectedPolicy ? '' : '请先选择存储策略'"
      />
    </div>
  </VModal>

  <AttachmentPolicyEditingModal
    v-if="visible"
    v-model:visible="policyEditingModal"
    :policy="policyToCreate"
    @close="onEditingModalClose"
  />
</template>
