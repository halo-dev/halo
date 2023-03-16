<script lang="ts" setup>
import {
  VPageHeader,
  VSpace,
  VButton,
  IconAddCircle,
  IconLockPasswordLine,
  VCard,
  IconArrowDown,
  VEntity,
  VEntityField,
  VTag,
  VStatusDot,
  VAvatar,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import type { AuthProvider } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { ref } from "vue";
import AuthProviderEditingModal from "./components/AuthProviderEditingModal.vue";

const {
  data: authProviders,
  isLoading,
  refetch,
} = useQuery<AuthProvider[]>({
  queryKey: ["auth-providers"],
  queryFn: async () => {
    const { data } =
      await apiClient.extension.authProvider.listauthHaloRunV1alpha1AuthProvider();
    return data.items;
  },
  refetchOnWindowFocus: false,
});

const selectedAuthProvider = ref<AuthProvider>();
const editingModal = ref(false);

const handleOpenEditingModal = (authProvider?: AuthProvider) => {
  selectedAuthProvider.value = authProvider;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  selectedAuthProvider.value = undefined;
  refetch();
};
</script>

<template>
  <AuthProviderEditingModal
    v-model:visible="editingModal"
    :auth-provider="selectedAuthProvider"
    @close="onEditingModalClose"
  />

  <VPageHeader title="身份认证">
    <template #icon>
      <IconLockPasswordLine class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton type="secondary">
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          添加认证方式
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col items-start sm:flex-row sm:items-center"
          >
            <div class="flex w-full flex-1 sm:w-auto">
              <FormKit placeholder="输入关键词搜索" type="text"></FormKit>
            </div>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(authProvider, index) in authProviders" :key="index">
            <VEntity>
              <template #start>
                <VEntityField>
                  <template #description>
                    <VAvatar
                      :alt="authProvider.spec.displayName"
                      :src="authProvider.spec.logo"
                      size="md"
                    ></VAvatar>
                  </template>
                </VEntityField>
                <VEntityField
                  :title="authProvider.spec.displayName"
                  :description="authProvider.spec.description"
                >
                  <template #extra>
                    <VTag>
                      {{ authProvider.spec.enabled ? "已启用" : "未启用" }}
                    </VTag>
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField v-if="authProvider.metadata.deletionTimestamp">
                  <template #description>
                    <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class="truncate text-xs tabular-nums text-gray-500">
                      {{
                        formatDatetime(authProvider.metadata.creationTimestamp)
                      }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template #dropdownItems>
                <VButton
                  v-close-popper
                  block
                  type="secondary"
                  @click="handleOpenEditingModal(authProvider)"
                >
                  编辑
                </VButton>
                <VButton v-close-popper block type="danger"> 删除 </VButton>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>
    </VCard>
  </div>
</template>
