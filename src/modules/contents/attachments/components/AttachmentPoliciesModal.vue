<script lang="ts" setup>
import {
  IconAddCircle,
  IconMore,
  VButton,
  VModal,
  VSpace,
  VEmpty,
} from "@halo-dev/components";
import AttachmentPolicyEditingModal from "./AttachmentPolicyEditingModal.vue";
import { ref, watch } from "vue";
import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";
import { v4 as uuid } from "uuid";
import { formatDatetime } from "@/utils/date";
import { useFetchAttachmentPolicy } from "../composables/use-attachment-policy";

const props = withDefaults(
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

const { policies, loading, handleFetchPolicies } = useFetchAttachmentPolicy();

const selectedPolicy = ref<Policy | null>(null);
const policyTemplates = ref<PolicyTemplate[]>([] as PolicyTemplate[]);

const policyEditingModal = ref(false);

const handleFetchPolicyTemplates = async () => {
  try {
    const { data } =
      await apiClient.extension.storage.policyTemplate.liststorageHaloRunV1alpha1PolicyTemplate();
    policyTemplates.value = data.items;
  } catch (e) {
    console.error("Failed to fetch attachment policy templates", e);
  }
};

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
      templateRef: {
        name: policyTemplate.metadata.name,
      },
      configMapRef: {
        name: uuid(),
      },
    },
    apiVersion: "storage.halo.run/v1alpha1",
    kind: "Policy",
    metadata: {
      name: uuid(),
    },
  };
  policyEditingModal.value = true;
};

const onEditingModalClose = () => {
  selectedPolicy.value = null;
  handleFetchPolicies();
};

watch(
  () => props.visible,
  (newValue) => {
    if (newValue) {
      handleFetchPolicyTemplates();
      handleFetchPolicies();
    }
  }
);
</script>
<template>
  <VModal
    :visible="visible"
    :width="750"
    title="存储策略"
    :body-class="['!p-0']"
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
      v-if="!policies.length && !loading"
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
        <div
          class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
        >
          <div class="relative flex flex-row items-center">
            <div class="flex-1">
              <div class="flex flex-col sm:flex-row">
                <span
                  class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                >
                  {{ policy.spec.displayName }}
                </span>
              </div>
              <div class="mt-1 flex">
                <span class="text-xs text-gray-500">
                  {{ policy.spec.templateRef?.name }}
                </span>
              </div>
            </div>
            <div class="flex">
              <div
                class="inline-flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
              >
                <time class="text-sm text-gray-500">
                  {{ formatDatetime(policy.metadata.creationTimestamp) }}
                </time>
                <span class="cursor-pointer">
                  <FloatingDropdown>
                    <IconMore />
                    <template #popper>
                      <div class="w-48 p-2">
                        <VSpace class="w-full" direction="column">
                          <VButton
                            v-close-popper
                            block
                            type="secondary"
                            @click="handleOpenEditingModal(policy)"
                          >
                            编辑
                          </VButton>
                          <VButton v-close-popper block type="danger">
                            删除
                          </VButton>
                        </VSpace>
                      </div>
                    </template>
                  </FloatingDropdown>
                </span>
              </div>
            </div>
          </div>
        </div>
      </li>
    </ul>
    <template #footer>
      <VButton @click="onVisibleChange(false)">关闭 Esc</VButton>
    </template>
  </VModal>

  <AttachmentPolicyEditingModal
    v-model:visible="policyEditingModal"
    :policy="selectedPolicy"
    @close="onEditingModalClose"
  />
</template>
