<script lang="ts" setup>
import { VModal } from "@halo-dev/components";
import FilePondUpload from "@/components/upload/FilePondUpload.vue";
import { computed, ref, watch, watchEffect } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
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

const { policies, handleFetchPolicies } = useFetchAttachmentPolicy();

const selectedPolicy = ref<Policy | null>(null);
const policyVisible = ref(false);
const FilePondUploadRef = ref();

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
    FilePondUploadRef.value.handleRemoveFiles();
  }
};

const uploadHandler = computed(() => {
  return (file, config) =>
    apiClient.extension.storage.attachment.uploadAttachment(
      file,
      selectedPolicy.value?.metadata.name as string,
      props.group?.metadata.name as string,
      config
    );
});

watch(
  () => props.visible,
  (newValue) => {
    if (newValue) {
      handleFetchPolicies();
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
    <template #actions>
      <FloatingDropdown>
        <div v-tooltip="`选择存储策略`" class="modal-header-action">
          <span class="text-sm">
            {{ selectedPolicy?.spec.displayName || "无存储策略" }}
          </span>
        </div>
        <template #popper>
          <div class="w-72 p-4">
            <ul class="space-y-1">
              <li
                v-for="(policy, index) in policies"
                :key="index"
                v-close-popper
                :class="{
                  '!bg-gray-100 !text-gray-900':
                    selectedPolicy?.metadata.name === policy.metadata.name,
                }"
                class="flex cursor-pointer flex-col rounded px-2 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                @click="selectedPolicy = policy"
              >
                <span class="truncate">
                  {{ policy.spec.displayName }}
                </span>
                <span class="text-xs">
                  {{ policy.spec.templateRef?.name }}
                </span>
              </li>
              <li
                v-close-popper
                class="flex cursor-pointer flex-col rounded px-2 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                @click="policyVisible = true"
              >
                <span class="truncate"> 新增存储策略 </span>
              </li>
            </ul>
          </div>
        </template>
      </FloatingDropdown>
    </template>
    <div class="w-full p-4">
      <FilePondUpload
        ref="FilePondUploadRef"
        :allow-multiple="true"
        :handler="uploadHandler"
        :disabled="!selectedPolicy"
        :max-parallel-uploads="5"
        :label-idle="
          selectedPolicy ? '点击选择文件或者拖拽文件到此处' : '请先选择存储策略'
        "
      />
    </div>
  </VModal>

  <AttachmentPoliciesModal
    v-model:visible="policyVisible"
    @close="handleFetchPolicies"
  />
</template>
