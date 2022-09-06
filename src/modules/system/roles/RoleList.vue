<script lang="ts" setup>
// core libs
import { ref } from "vue";
import type { Role } from "@halo-dev/api-client";

// components
import {
  IconAddCircle,
  IconArrowDown,
  IconSettings,
  IconShieldUser,
  useDialog,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
} from "@halo-dev/components";
import RoleEditingModal from "./components/RoleEditingModal.vue";

// constants
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";

// hooks
import { useFetchRole } from "@/modules/system/roles/composables/use-role";

// libs
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@halo-dev/admin-shared";

const editingModal = ref<boolean>(false);
const selectedRole = ref<Role | null>(null);

const { roles, handleFetchRoles } = useFetchRole();

const handleOpenEditingModal = (role: Role) => {
  selectedRole.value = role;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  selectedRole.value = null;
  handleFetchRoles();
};

const handleCloneRole = (role: Role) => {
  const roleToCreate = cloneDeep<Role>(role);
  roleToCreate.metadata.name = "";
  roleToCreate.metadata.creationTimestamp = undefined;
  selectedRole.value = roleToCreate;
  editingModal.value = true;
};

const dialog = useDialog();
const handleDelete = async (role: Role) => {
  dialog.warning({
    title: "是否确定删除该权限？",
    description: "此权限删除之后，相关联的用户将被删除角色绑定，此操作不可恢复",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.role.deletev1alpha1Role({
          name: role.metadata.name,
        });
      } catch (e) {
        console.error("Failed to delete role", e);
      }
    },
  });
};
</script>
<template>
  <RoleEditingModal
    v-model:visible="editingModal"
    :role="selectedRole"
    @close="onEditingModalClose"
  />

  <VPageHeader title="角色">
    <template #icon>
      <IconShieldUser class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton
        v-permission="['system:roles:manage']"
        type="secondary"
        @click="editingModal = true"
      >
        <template #icon>
          <IconAddCircle class="h-full w-full" />
        </template>
        新建角色
      </VButton>
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
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">状态</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-52 p-4">
                      <ul class="space-y-1">
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">正常</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">已禁用</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">类型</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-52 p-4">
                      <ul class="space-y-1">
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">系统保留</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">自定义</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">排序</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-72 p-4">
                      <ul class="space-y-1">
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">更高权限</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">更低权限</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
              </VSpace>
            </div>
          </div>
        </div>
      </template>
      <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
        <li v-for="(role, index) in roles" :key="index">
          <div
            class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div class="relative flex flex-row items-center">
              <div class="flex-1">
                <div class="flex flex-row items-center">
                  <RouterLink
                    :to="{
                      name: 'RoleDetail',
                      params: {
                        name: role.metadata.name,
                      },
                    }"
                  >
                    <span
                      class="mr-2 truncate text-sm font-medium text-gray-900"
                    >
                      {{
                        role.metadata.annotations?.[
                          rbacAnnotations.DISPLAY_NAME
                        ] || role.metadata.name
                      }}
                    </span>
                  </RouterLink>
                </div>
                <div class="mt-2 flex">
                  <span class="text-xs text-gray-500">
                    包含
                    {{
                      JSON.parse(
                        role.metadata.annotations?.[
                          rbacAnnotations.DEPENDENCIES
                        ] || "[]"
                      ).length
                    }}
                    个权限
                  </span>
                </div>
              </div>
              <div class="flex">
                <div
                  class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                >
                  <FloatingTooltip
                    v-if="role.metadata.deletionTimestamp"
                    class="hidden items-center sm:flex"
                  >
                    <div
                      class="inline-flex h-1.5 w-1.5 rounded-full bg-red-600"
                    >
                      <span
                        class="inline-block h-1.5 w-1.5 animate-ping rounded-full bg-red-600"
                      ></span>
                    </div>
                    <template #popper> 删除中</template>
                  </FloatingTooltip>

                  <a
                    class="hidden text-sm text-gray-500 hover:text-gray-900 sm:block"
                    target="_blank"
                  >
                    0 个用户
                  </a>
                  <VTag> 系统保留</VTag>
                  <time class="text-sm text-gray-500">
                    {{ formatDatetime(role.metadata.creationTimestamp) }}
                  </time>
                  <span
                    v-permission="['system:roles:manage']"
                    class="cursor-pointer"
                  >
                    <FloatingDropdown>
                      <IconSettings />
                      <template #popper>
                        <div class="w-48 p-2">
                          <VSpace class="w-full" direction="column">
                            <VButton
                              v-close-popper
                              block
                              type="secondary"
                              @click="handleOpenEditingModal(role)"
                            >
                              编辑
                            </VButton>
                            <VButton
                              v-close-popper
                              block
                              type="danger"
                              @click="handleDelete(role)"
                            >
                              删除
                            </VButton>
                            <VButton
                              v-close-popper
                              block
                              @click="handleCloneRole(role)"
                            >
                              基于此角色创建
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
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination :page="1" :size="10" :total="20" />
        </div>
      </template>
    </VCard>
  </div>
</template>
