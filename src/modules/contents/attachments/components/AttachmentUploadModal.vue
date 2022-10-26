<script lang="ts" setup>
import {
  VModal,
  VEmpty,
  IconAddCircle,
  VButton,
  VSpace,
} from "@halo-dev/components";
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { computed, ref, watch, watchEffect } from "vue";
import type { Policy, Group } from "@halo-dev/api-client";
import { useFetchAttachmentPolicy } from "../composables/use-attachment-policy";
import AttachmentPoliciesModal from "./AttachmentPoliciesModal.vue";

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

const { policies, loading, handleFetchPolicies } = useFetchAttachmentPolicy();

const selectedPolicy = ref<Policy | null>(null);
const policyVisible = ref(false);
const uploadVisible = ref(false);

const modalTitle = computed(() => {
  if (props.group && props.group.metadata.name) {
    return `上传附件：${props.group.spec.displayName}`;
  }
  return "上传附件";
});

watchEffect(() => {
  if (policies.value.length) {
    selectedPolicy.value = policies.value[0];
  }
});

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
    policyVisible.value = false;
  }
};

watch(
  () => props.visible,
  (newValue) => {
    if (newValue) {
      handleFetchPolicies();
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
    :width="600"
    :title="modalTitle"
    @update:visible="onVisibleChange"
  >
    <VEmpty
      v-if="!policies.length && !loading"
      message="当前没有上传附件的存储策略，请先创建存储策略"
      title="无存储策略"
    >
      <template #actions>
        <VSpace>
          <VButton @click="handleFetchPolicies">刷新</VButton>
          <VButton type="secondary" @click="policyVisible = true">
            <template #icon>
              <IconAddCircle class="h-full w-full" />
            </template>
            新建策略
          </VButton>
        </VSpace>
      </template>
    </VEmpty>
    <div v-else class="w-full p-4">
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
        <div
          class="flex cursor-pointer items-center rounded-base bg-gray-100 p-2 text-gray-500 transition-all hover:bg-gray-200 hover:text-gray-900 hover:shadow-sm"
          @click="policyVisible = true"
        >
          <div class="flex flex-1 items-center truncate">
            <span class="truncate text-sm">新建策略</span>
          </div>
          <IconAddCircle />
        </div>
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

  <AttachmentPoliciesModal
    v-model:visible="policyVisible"
    @close="handleFetchPolicies"
  />
</template>
