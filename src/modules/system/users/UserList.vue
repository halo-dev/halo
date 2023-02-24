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
  Dialog,
  VStatusDot,
  VLoading,
  Toast,
  IconRefreshLine,
  VEmpty,
} from "@halo-dev/components";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import GrantPermissionModal from "./components/GrantPermissionModal.vue";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import type { Role, User, ListedUserList } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";
import { useRouteQuery } from "@vueuse/router";
import { usePermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";
import { getNode } from "@formkit/core";
import FilterTag from "@/components/filter/FilterTag.vue";
import { useFetchRole } from "../roles/composables/use-role";
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";

const { currentUserHasPermission } = usePermission();

const checkedAll = ref(false);
const editingModal = ref<boolean>(false);
const passwordChangeModal = ref<boolean>(false);
const grantPermissionModal = ref<boolean>(false);

const users = ref<ListedUserList>({
  page: 1,
  size: 20,
  total: 0,
  items: [],
  first: true,
  last: false,
  hasNext: false,
  hasPrevious: false,
  totalPages: 0,
});
const loading = ref(false);
const selectedUserNames = ref<string[]>([]);
const selectedUser = ref<User>();
const keyword = ref("");
const refreshInterval = ref();

const userStore = useUserStore();

const ANONYMOUSUSER_NAME = "anonymousUser";
const DELETEDUSER_NAME = "ghost";

const handleFetchUsers = async (options?: {
  mute?: boolean;
  page?: number;
}) => {
  try {
    clearInterval(refreshInterval.value);

    if (!options?.mute) {
      loading.value = true;
    }

    if (options?.page) {
      users.value.page = options.page;
    }

    const { data } = await apiClient.user.listUsers({
      page: users.value.page,
      size: users.value.size,
      keyword: keyword.value,
      fieldSelector: [
        `name!=${ANONYMOUSUSER_NAME}`,
        `name!=${DELETEDUSER_NAME}`,
      ],
      sort: [selectedSortItem.value?.value].filter(
        (item) => !!item
      ) as string[],
      role: selectedRole.value?.metadata.name,
    });

    users.value = data;

    const deletedUsers = users.value.items.filter(
      (user) => !!user.user.metadata.deletionTimestamp
    );

    if (deletedUsers.length) {
      refreshInterval.value = setInterval(() => {
        handleFetchUsers({ mute: true });
      }, 3000);
    }
  } catch (e) {
    console.error("Failed to fetch users", e);
  } finally {
    selectedUser.value = undefined;
    loading.value = false;
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
  Dialog.warning({
    title: "确定要删除该用户吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.user.deletev1alpha1User({
          name: user.metadata.name,
        });

        Toast.success("删除成功");
      } catch (e) {
        console.error("Failed to delete user", e);
      } finally {
        await handleFetchUsers();
      }
    },
  });
};

const handleDeleteInBatch = async () => {
  Dialog.warning({
    title: "确定要删除选中的用户吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      const userNamesToDelete = selectedUserNames.value.filter(
        (name) => name != userStore.currentUser?.metadata.name
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
      Toast.success("删除成功");
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
        return user.user.metadata.name;
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

const handleOpenGrantPermissionModal = (user: User) => {
  selectedUser.value = user;
  grantPermissionModal.value = true;
};

onMounted(() => {
  handleFetchUsers();
});

onUnmounted(() => {
  clearInterval(refreshInterval.value);
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

// Filters
function handleKeywordChange() {
  const keywordNode = getNode("keywordInput");
  if (keywordNode) {
    keyword.value = keywordNode._value as string;
  }
  handleFetchUsers({ page: 1 });
}

function handleClearKeyword() {
  keyword.value = "";
  handleFetchUsers({ page: 1 });
}

interface SortItem {
  label: string;
  value: string;
}

const SortItems: SortItem[] = [
  {
    label: "较近创建",
    value: "creationTimestamp,desc",
  },
  {
    label: "较早创建",
    value: "creationTimestamp,asc",
  },
];

const selectedSortItem = ref<SortItem>();

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  handleFetchUsers({ page: 1 });
}

const { roles } = useFetchRole();
const selectedRole = ref<Role>();

function handleRoleChange(role?: Role) {
  selectedRole.value = role;
  handleFetchUsers({ page: 1 });
}

function handleClearFilters() {
  selectedRole.value = undefined;
  selectedSortItem.value = undefined;
  keyword.value = "";
  handleFetchUsers({ page: 1 });
}

const hasFilters = computed(() => {
  return selectedRole.value || selectedSortItem.value || keyword.value;
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

  <GrantPermissionModal
    v-model:visible="grantPermissionModal"
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
            <div
              v-permission="['system:users:manage']"
              class="mr-4 hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <div
                v-if="!selectedUserNames.length"
                class="flex items-center gap-2"
              >
                <FormKit
                  id="keywordInput"
                  outer-class="!p-0"
                  :model-value="keyword"
                  name="keyword"
                  placeholder="输入关键词搜索"
                  type="text"
                  @keyup.enter="handleKeywordChange"
                ></FormKit>

                <FilterTag v-if="keyword" @close="handleClearKeyword()">
                  关键词：{{ keyword }}
                </FilterTag>

                <FilterTag v-if="selectedRole" @close="handleRoleChange()">
                  角色：{{
                    selectedRole.metadata.annotations?.[
                      rbacAnnotations.DISPLAY_NAME
                    ] || selectedRole.metadata.name
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedSortItem"
                  @close="handleSortItemChange()"
                >
                  排序：{{ selectedSortItem.label }}
                </FilterTag>

                <FilterCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
              </div>
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
                    <span class="mr-0.5">角色</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-52 p-4">
                      <ul class="space-y-1">
                        <li
                          v-for="(role, index) in roles"
                          :key="index"
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleRoleChange(role)"
                        >
                          <span class="truncate">
                            {{
                              role.metadata.annotations?.[
                                rbacAnnotations.DISPLAY_NAME
                              ] || role.metadata.name
                            }}
                          </span>
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
                          v-for="(sortItem, index) in SortItems"
                          :key="index"
                          v-close-popper
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleSortItemChange(sortItem)"
                        >
                          <span class="truncate">{{ sortItem.label }}</span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="handleFetchUsers()"
                  >
                    <IconRefreshLine
                      v-tooltip="`刷新`"
                      :class="{ 'animate-spin text-gray-900': loading }"
                      class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                    />
                  </div>
                </div>
              </VSpace>
            </div>
          </div>
        </div>
      </template>

      <VLoading v-if="loading" />

      <Transition v-else-if="!users.total" appear name="fade">
        <VEmpty
          message="当前没有符合筛选条件的用户，你可以尝试刷新或者创建新用户"
          title="当前没有符合筛选条件的用户"
        >
          <template #actions>
            <VSpace>
              <VButton @click="handleFetchUsers()">刷新</VButton>
              <VButton
                v-permission="['system:users:manage']"
                type="secondary"
                @click="editingModal = true"
              >
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                新建用户
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(user, index) in users.items" :key="index">
            <VEntity :is-selected="checkSelection(user.user)">
              <template
                v-if="currentUserHasPermission(['system:users:manage'])"
                #checkbox
              >
                <input
                  v-model="selectedUserNames"
                  :value="user.user.metadata.name"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  name="post-checkbox"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField>
                  <template #description>
                    <VAvatar
                      :alt="user.user.spec.displayName"
                      :src="user.user.spec.avatar"
                      size="md"
                    ></VAvatar>
                  </template>
                </VEntityField>
                <VEntityField
                  :title="user.user.spec.displayName"
                  :description="user.user.metadata.name"
                  :route="{
                    name: 'UserDetail',
                    params: { name: user.user.metadata.name },
                  }"
                />
              </template>
              <template #end>
                <VEntityField>
                  <template #description>
                    <div
                      v-for="(role, roleIndex) in user.roles"
                      :key="roleIndex"
                      class="flex items-center"
                    >
                      <VTag>
                        {{
                          role.metadata.annotations?.[
                            rbacAnnotations.DISPLAY_NAME
                          ] || role.metadata.name
                        }}
                      </VTag>
                    </div>
                  </template>
                </VEntityField>
                <VEntityField v-if="user.user.metadata.deletionTimestamp">
                  <template #description>
                    <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class="truncate text-xs tabular-nums text-gray-500">
                      {{ formatDatetime(user.user.metadata.creationTimestamp) }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template
                v-if="currentUserHasPermission(['system:users:manage'])"
                #dropdownItems
              >
                <VButton
                  v-close-popper
                  block
                  type="secondary"
                  @click="handleOpenCreateModal(user.user)"
                >
                  修改资料
                </VButton>
                <VButton
                  v-close-popper
                  block
                  @click="handleOpenPasswordChangeModal(user.user)"
                >
                  修改密码
                </VButton>
                <VButton
                  v-if="
                    userStore.currentUser?.metadata.name !==
                    user.user.metadata.name
                  "
                  v-close-popper
                  block
                  @click="handleOpenGrantPermissionModal(user.user)"
                >
                  分配角色
                </VButton>
                <VButton
                  v-if="
                    userStore.currentUser?.metadata.name !==
                    user.user.metadata.name
                  "
                  v-close-popper
                  block
                  type="danger"
                  @click="handleDelete(user.user)"
                >
                  删除
                </VButton>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            :page="users.page"
            :size="users.size"
            :total="users.total"
            :size-options="[20, 30, 50, 100]"
            @change="handlePaginationChange"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
