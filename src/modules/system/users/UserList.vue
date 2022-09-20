<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconUserFollow,
  IconUserSettings,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
  VAvatar,
  VEntity,
  VEntityField,
  useDialog,
  VStatusDot,
} from "@halo-dev/components";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import { inject, onMounted, ref, watch } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type { User, UserList } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";
import { useRouteQuery } from "@vueuse/router";

const dialog = useDialog();

const checkedAll = ref(false);
const editingModal = ref<boolean>(false);
const passwordChangeModal = ref<boolean>(false);
const users = ref<UserList>({
  page: 1,
  size: 10,
  total: 0,
  items: [],
  first: true,
  last: false,
  hasNext: false,
  hasPrevious: false,
});
const selectedUserNames = ref<string[]>([]);
const selectedUser = ref<User | null>(null);
const currentUser = inject<User>("currentUser");

const handleFetchUsers = async () => {
  try {
    const { data } = await apiClient.extension.user.listv1alpha1User({
      page: users.value.page,
      size: users.value.size,
    });
    users.value = data;
  } catch (e) {
    console.error(e);
  } finally {
    selectedUser.value = null;
  }
};

const handlePaginationChange = async ({
  page,
  size,
}: {
  page: number;
  size: number;
}) => {
  users.value.page = page;
  users.value.size = size;
  await handleFetchUsers();
};

const handleDelete = async (user: User) => {
  dialog.warning({
    title: "确定要删除该用户吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.user.deletev1alpha1User({
          name: user.metadata.name,
        });
      } catch (e) {
        console.error("Failed to delete user", e);
      } finally {
        await handleFetchUsers();
      }
    },
  });
};

const handleDeleteInBatch = async () => {
  dialog.warning({
    title: "是否确认删除选中的用户？",
    confirmType: "danger",
    onConfirm: async () => {
      const userNamesToDelete = selectedUserNames.value.filter(
        (name) => name != currentUser?.metadata.name
      );
      await Promise.all(
        userNamesToDelete.map((name) => {
          return apiClient.extension.user.deletev1alpha1User({
            name,
          });
        })
      );
      await handleFetchUsers();
      selectedUserNames.value.length = 0;
    },
  });
};

watch(selectedUserNames, (newValue) => {
  checkedAll.value = newValue.length === users.value.items?.length;
});

const checkSelection = (user: User) => {
  return (
    user.metadata.name === selectedUser.value?.metadata.name ||
    selectedUserNames.value.includes(user.metadata.name)
  );
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedUserNames.value =
      users.value.items.map((user) => {
        return user.metadata.name;
      }) || [];
  } else {
    selectedUserNames.value.length = 0;
  }
};

const handleOpenCreateModal = (user: User) => {
  selectedUser.value = user;
  editingModal.value = true;
};

const onEditingModalClose = () => {
  routeQueryAction.value = undefined;
  handleFetchUsers();
};

const handleOpenPasswordChangeModal = (user: User) => {
  selectedUser.value = user;
  passwordChangeModal.value = true;
};

const getRoles = (user: User) => {
  return JSON.parse(
    user.metadata.annotations?.[rbacAnnotations.ROLE_NAMES] || "[]"
  );
};

onMounted(() => {
  handleFetchUsers();
});

// Route query action
const routeQueryAction = useRouteQuery<string | undefined>("action");

onMounted(() => {
  if (!routeQueryAction.value) {
    return;
  }
  if (routeQueryAction.value === "create") {
    editingModal.value = true;
  }
});
</script>
<template>
  <UserEditingModal
    v-model:visible="editingModal"
    :user="selectedUser"
    @close="onEditingModalClose"
  />

  <UserPasswordChangeModal
    v-model:visible="passwordChangeModal"
    :user="selectedUser"
    @close="handleFetchUsers"
  />

  <VPageHeader title="用户">
    <template #icon>
      <IconUserSettings class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton
          v-permission="['system:roles:view']"
          :route="{ name: 'Roles' }"
          size="sm"
          type="default"
        >
          <template #icon>
            <IconUserFollow class="h-full w-full" />
          </template>
          角色管理
        </VButton>
        <VButton
          v-permission="['system:users:manage']"
          type="secondary"
          @click="editingModal = true"
        >
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          添加用户
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
            <div class="mr-4 hidden items-center sm:flex">
              <input
                v-model="checkedAll"
                v-permission="['system:users:manage']"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 sm:w-auto">
              <FormKit
                v-if="!selectedUserNames.length"
                placeholder="输入关键词搜索"
                type="text"
              ></FormKit>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  删除
                </VButton>
              </VSpace>
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
                    <span class="mr-0.5">角色</span>
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
                          <span class="truncate">Super Administrator</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">Administrator</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">Editor</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">Guest</span>
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
                          <span class="truncate">较近登录</span>
                        </li>
                        <li
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">较晚登录</span>
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
        <li v-for="(user, index) in users.items" :key="index">
          <VEntity :is-selected="checkSelection(user)">
            <template #checkbox>
              <input
                v-model="selectedUserNames"
                v-permission="['system:users:manage']"
                :value="user.metadata.name"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                name="post-checkbox"
                type="checkbox"
              />
            </template>
            <template #start>
              <VEntityField>
                <template #description>
                  <VAvatar
                    :alt="user.spec.displayName"
                    :src="user.spec.avatar"
                    size="md"
                  ></VAvatar>
                </template>
              </VEntityField>
              <VEntityField
                :title="user.spec.displayName"
                :description="user.metadata.name"
                :route="{
                  name: 'UserDetail',
                  params: { name: user.metadata.name },
                }"
              />
            </template>
            <template #end>
              <VEntityField>
                <template #description>
                  <div
                    v-for="(role, roleIndex) in getRoles(user)"
                    :key="roleIndex"
                    class="flex items-center"
                  >
                    <VTag>
                      {{ role }}
                    </VTag>
                  </div>
                </template>
              </VEntityField>
              <VEntityField v-if="user.metadata.deletionTimestamp">
                <template #description>
                  <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                </template>
              </VEntityField>
              <VEntityField
                :description="formatDatetime(user.metadata.creationTimestamp)"
              />
            </template>
            <template #dropdownItems>
              <VButton
                v-close-popper
                v-permission="['system:users:manage']"
                block
                type="secondary"
                @click="handleOpenCreateModal(user)"
              >
                修改资料
              </VButton>
              <VButton
                v-close-popper
                v-permission="['system:users:manage']"
                block
                @click="handleOpenPasswordChangeModal(user)"
              >
                修改密码
              </VButton>
              <VButton
                v-if="currentUser?.metadata.name !== user.metadata.name"
                v-close-popper
                v-permission="['system:users:manage']"
                block
                type="danger"
                @click="handleDelete(user)"
              >
                删除
              </VButton>
            </template>
          </VEntity>
        </li>
      </ul>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            :page="users.page"
            :size="users.size"
            :total="users.total"
            @change="handlePaginationChange"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
