<script lang="ts" setup>
// core libs
import { computed, ref, watch } from "vue";
import type { Role } from "@halo-dev/api-client";

// components
import {
  IconAddCircle,
  IconArrowDown,
  IconShieldUser,
  useDialog,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
  VStatusDot,
  VEntity,
  VEntityField,
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

const editingModal = ref<boolean>(false);
const selectedRole = ref<Role>();

const { roles, handleFetchRoles } = useFetchRole();

let fuse: Fuse<Role> | undefined = undefined;

watch(
  () => roles.value,
  (value) => {
    fuse = new Fuse(value, {
      keys: ["spec.displayName", "metadata.name"],
      useExtendedSearch: true,
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

const handleOpenEditingModal = (role: Role) => {
  selectedRole.value = role;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  selectedRole.value = undefined;
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
      <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
        <li v-for="(role, index) in searchResults" :key="index">
          <VEntity>
            <template #start>
              <VEntityField
                :title="
                  role.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
                  role.metadata.name
                "
                :description="`包含
                    ${
                      JSON.parse(
                        role.metadata.annotations?.[
                          rbacAnnotations.DEPENDENCIES
                        ] || '[]'
                      ).length
                    }
                    个权限`"
                :route="{
                  name: 'RoleDetail',
                  params: {
                    name: role.metadata.name,
                  },
                }"
              ></VEntityField>
            </template>
            <template #end>
              <VEntityField v-if="role.metadata.deletionTimestamp">
                <template #description>
                  <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                </template>
              </VEntityField>
              <VEntityField description="0 个用户" />
              <VEntityField>
                <template #description>
                  <VTag> 系统保留</VTag>
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
            <template #dropdownItems>
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
              <VButton v-close-popper block @click="handleCloneRole(role)">
                基于此角色创建
              </VButton>
            </template>
          </VEntity>
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
