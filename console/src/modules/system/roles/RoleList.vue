<script lang="ts" setup>
// core libs
import { computed, ref, watch } from "vue";
import type { Role } from "@halo-dev/api-client";

// components
import {
  IconAddCircle,
  IconArrowDown,
  IconShieldUser,
  Dialog,
  VButton,
  VCard,
  VPageHeader,
  VSpace,
  VTag,
  VStatusDot,
  VEntity,
  VEntityField,
  VLoading,
  Toast,
} from "@halo-dev/components";
import RoleEditingModal from "./components/RoleEditingModal.vue";

// constants
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";

// hooks
import { useFetchRole } from "@/modules/system/roles/composables/use-role";

// libs
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import Fuse from "fuse.js";
import { usePermission } from "@/utils/permission";
import { roleLabels } from "@/constants/labels";
import { SUPER_ROLE_NAME } from "@/constants/constants";

const { currentUserHasPermission } = usePermission();

const editingModal = ref<boolean>(false);
const selectedRole = ref<Role>();

const { roles, handleFetchRoles, loading } = useFetchRole();

let fuse: Fuse<Role> | undefined = undefined;

watch(
  () => roles.value,
  (value) => {
    fuse = new Fuse(value, {
      keys: ["spec.displayName", "metadata.name"],
      useExtendedSearch: true,
      threshold: 0.2,
    });
  }
);

const keyword = ref("");

const searchResults = computed(() => {
  if (!fuse || !keyword.value) {
    return roles.value;
  }

  return fuse?.search(keyword.value).map((item) => item.item);
});

const isSystemReserved = (role: Role) => {
  return role.metadata.labels?.[roleLabels.SYSTEM_RESERVED] === "true";
};

const getRoleCountText = (role: Role) => {
  if (role.metadata.name === SUPER_ROLE_NAME) {
    return `包含所有权限`;
  }

  const dependenciesCount = JSON.parse(
    role.metadata.annotations?.[rbacAnnotations.DEPENDENCIES] || "[]"
  ).length;

  return `包含 ${dependenciesCount} 个权限`;
};

const handleOpenEditingModal = (role: Role) => {
  selectedRole.value = role;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  selectedRole.value = undefined;
  handleFetchRoles();
};

const handleCloneRole = async (role: Role) => {
  const roleToCreate = cloneDeep<Role>(role);
  roleToCreate.metadata.name = "";
  roleToCreate.metadata.generateName = "role-";
  roleToCreate.metadata.creationTimestamp = undefined;

  // 移除系统保留标识
  delete roleToCreate.metadata.labels?.[roleLabels.SYSTEM_RESERVED];

  // 如果是超级管理员角色，那么需要获取到所有角色模板并填充到表单
  if (role.metadata.name === SUPER_ROLE_NAME) {
    const { data } = await apiClient.extension.role.listv1alpha1Role({
      page: 0,
      size: 0,
      labelSelector: [`${roleLabels.TEMPLATE}=true`, "!halo.run/hidden"],
    });
    const roleTemplateNames = data.items.map((item) => item.metadata.name);
    if (roleToCreate.metadata.annotations) {
      roleToCreate.metadata.annotations[rbacAnnotations.DEPENDENCIES] =
        JSON.stringify(roleTemplateNames);
    } else {
      roleToCreate.metadata.annotations = {
        [rbacAnnotations.DEPENDENCIES]: JSON.stringify(roleTemplateNames),
      };
    }
  }

  selectedRole.value = roleToCreate;
  editingModal.value = true;
};

const handleDelete = async (role: Role) => {
  Dialog.warning({
    title: "确定要删除该角色吗？",
    description: "该角色删除后，相关联的用户将被删除角色绑定，该操作不可恢复",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.role.deletev1alpha1Role({
          name: role.metadata.name,
        });

        Toast.success("删除成功");
      } catch (e) {
        console.error("Failed to delete role", e);
      } finally {
        handleFetchRoles();
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
              <FormKit
                v-model="keyword"
                placeholder="输入关键词搜索"
                type="text"
              ></FormKit>
            </div>
            <div v-if="false" class="mt-4 flex sm:mt-0">
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
      <VLoading v-if="loading" />
      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(role, index) in searchResults" :key="index">
            <VEntity>
              <template #start>
                <VEntityField
                  :title="
                    role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
                    role.metadata.name
                  "
                  :description="getRoleCountText(role)"
                  :route="{
                    name: 'RoleDetail',
                    params: {
                      name: role.metadata.name,
                    },
                  }"
                ></VEntityField>
              </template>
              <template #end>
                <!-- TODO: 支持显示用户数量 -->
                <VEntityField v-if="false" description="0 个用户" />
                <VEntityField>
                  <template #description>
                    <VTag>
                      {{ isSystemReserved(role) ? "系统保留" : "自定义" }}
                    </VTag>
                  </template>
                </VEntityField>
                <VEntityField v-if="role.metadata.deletionTimestamp">
                  <template #description>
                    <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class="truncate text-xs tabular-nums text-gray-500">
                      {{ formatDatetime(role.metadata.creationTimestamp) }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template
                v-if="currentUserHasPermission(['system:roles:manage'])"
                #dropdownItems
              >
                <VButton
                  v-if="!isSystemReserved(role)"
                  v-close-popper
                  block
                  type="secondary"
                  @click="handleOpenEditingModal(role)"
                >
                  编辑
                </VButton>
                <VButton
                  v-if="!isSystemReserved(role)"
                  v-close-popper
                  block
                  type="danger"
                  @click="handleDelete(role)"
                >
                  删除
                </VButton>
                <VButton v-close-popper block @click="handleCloneRole(role)">
                  基于此角色创建
                </VButton>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>
    </VCard>
  </div>
</template>
