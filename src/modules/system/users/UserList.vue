<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconSettings,
  IconUserFollow,
  IconUserSettings,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  VTag,
} from "@halo-dev/components";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import { onMounted, ref } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type { User, UserList } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";

const checkAll = ref(false);
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
const selectedUser = ref<User | null>(null);

const handleFetchUsers = async () => {
  try {
    const { data } = await apiClient.extension.user.listv1alpha1User(
      users.value.page,
      users.value.size
    );
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

const handleOpenCreateModal = (user: User) => {
  selectedUser.value = user;
  editingModal.value = true;
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
</script>
<template>
  <UserEditingModal
    v-model:visible="editingModal"
    v-permission="['system:users:manage']"
    :user="selectedUser"
    @close="handleFetchUsers"
  />

  <UserPasswordChangeModal
    v-model:visible="passwordChangeModal"
    v-permission="['system:users:manage']"
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
                v-model="checkAll"
                v-permission="['system:users:manage']"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
              />
            </div>
            <div class="flex w-full flex-1 sm:w-auto">
              <FormKit
                v-if="!checkAll"
                placeholder="输入关键词搜索"
                type="text"
              ></FormKit>
              <VSpace v-else>
                <VButton type="default">设置</VButton>
                <VButton type="danger">删除</VButton>
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
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">正常</span>
                        </li>
                        <li
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
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">Super Administrator</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">Administrator</span>
                        </li>
                        <li
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">Editor</span>
                        </li>
                        <li
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
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        >
                          <span class="truncate">较近登录</span>
                        </li>
                        <li
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
          <div
            :class="{
              'bg-gray-100': checkAll,
            }"
            class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
          >
            <div
              v-show="checkAll"
              class="absolute inset-y-0 left-0 w-0.5 bg-primary"
            ></div>
            <div class="relative flex flex-row items-center">
              <div class="mr-4 hidden items-center sm:flex">
                <input
                  v-model="checkAll"
                  v-permission="['system:users:manage']"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  type="checkbox"
                />
              </div>
              <div v-if="user.spec.avatar" class="mr-4">
                <div class="h-12 w-12">
                  <img
                    :alt="user.spec.displayName"
                    :src="user.spec.avatar"
                    class="h-full w-full overflow-hidden rounded border bg-white hover:shadow-sm"
                  />
                </div>
              </div>
              <div class="flex-1">
                <div class="flex flex-row items-center">
                  <span
                    class="mr-2 truncate text-sm font-medium text-gray-900"
                    @click="
                      $router.push({
                        name: 'UserDetail',
                        params: { name: user.metadata.name },
                      })
                    "
                  >
                    {{ user.spec.displayName }}
                  </span>
                  <VTag class="sm:hidden">{{ user.metadata.name }}</VTag>
                </div>
                <div class="mt-1 flex">
                  <VSpace align="start" direction="column" spacing="xs">
                    <span class="text-xs text-gray-500">
                      {{ user.metadata.name }}
                    </span>
                  </VSpace>
                </div>
              </div>
              <div class="flex">
                <div
                  class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
                >
                  <div
                    v-for="(role, index) in getRoles(user)"
                    :key="index"
                    class="hidden items-center sm:flex"
                  >
                    <VTag>
                      {{ role }}
                    </VTag>
                  </div>
                  <time class="text-sm text-gray-500" datetime="2020-01-07">
                    {{ user.metadata.creationTimestamp }}
                  </time>
                  <span
                    v-permission="['system:users:manage']"
                    class="cursor-pointer"
                  >
                    <FloatingDropdown>
                      <IconSettings />
                      <template #popper>
                        <div class="links-w-48 links-p-2">
                          <VSpace class="links-w-full" direction="column">
                            <VButton
                              block
                              type="secondary"
                              @click="handleOpenCreateModal(user)"
                            >
                              修改资料
                            </VButton>
                            <VButton
                              block
                              @click="handleOpenPasswordChangeModal(user)"
                            >
                              修改密码
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
