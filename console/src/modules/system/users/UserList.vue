<script lang="ts" setup>
import {
  IconAddCircle,
  IconArrowDown,
  IconUserFollow,
  IconUserSettings,
  IconLockPasswordLine,
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
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import GrantPermissionModal from "./components/GrantPermissionModal.vue";
import { computed, onMounted, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import type { Role, User, ListedUser } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";
import { useRouteQuery } from "@vueuse/router";
import { usePermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";
import { getNode } from "@formkit/core";
import FilterTag from "@/components/filter/FilterTag.vue";
import { useFetchRole } from "../roles/composables/use-role";
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import UserCreationModal from "./components/UserCreationModal.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const checkedAll = ref(false);
const editingModal = ref<boolean>(false);
const creationModal = ref<boolean>(false);
const passwordChangeModal = ref<boolean>(false);
const grantPermissionModal = ref<boolean>(false);

const selectedUserNames = ref<string[]>([]);
const selectedUser = ref<User>();
const keyword = ref("");

const userStore = useUserStore();

const ANONYMOUSUSER_NAME = "anonymousUser";
const DELETEDUSER_NAME = "ghost";

// Filters
function handleKeywordChange() {
  const keywordNode = getNode("keywordInput");
  if (keywordNode) {
    keyword.value = keywordNode._value as string;
  }
  page.value = 1;
}

function handleClearKeyword() {
  keyword.value = "";
  page.value = 1;
}

interface SortItem {
  label: string;
  value: string;
}

const SortItems: SortItem[] = [
  {
    label: t("core.user.filters.sort.items.create_time_desc"),
    value: "creationTimestamp,desc",
  },
  {
    label: t("core.user.filters.sort.items.create_time_asc"),
    value: "creationTimestamp,asc",
  },
];

const selectedSortItem = ref<SortItem>();

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  page.value = 1;
}

const { roles } = useFetchRole();
const selectedRole = ref<Role>();

function handleRoleChange(role?: Role) {
  selectedRole.value = role;
  page.value = 1;
}

function handleClearFilters() {
  selectedRole.value = undefined;
  selectedSortItem.value = undefined;
  keyword.value = "";
  page.value = 1;
}

const hasFilters = computed(() => {
  return selectedRole.value || selectedSortItem.value || keyword.value;
});

const page = ref(1);
const size = ref(20);
const total = ref(0);

const {
  data: users,
  isLoading,
  isFetching,
  refetch,
} = useQuery<ListedUser[]>({
  queryKey: ["users", page, size, keyword, selectedSortItem, selectedRole],
  queryFn: async () => {
    const { data } = await apiClient.user.listUsers({
      page: page.value,
      size: size.value,
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

    total.value = data.total;

    return data.items;
  },
  refetchInterval(data) {
    const deletingUsers = data?.filter(
      (user) => !!user.user.metadata.deletionTimestamp
    );

    return deletingUsers?.length ? 3000 : false;
  },
  onSuccess() {
    selectedUser.value = undefined;
  },
});

const handleDelete = async (user: User) => {
  Dialog.warning({
    title: t("core.user.operations.delete.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await apiClient.extension.user.deletev1alpha1User({
          name: user.metadata.name,
        });

        Toast.success(t("core.common.toast.delete_success"));
      } catch (e) {
        console.error("Failed to delete user", e);
      } finally {
        await refetch();
      }
    },
  });
};

const handleDeleteInBatch = async () => {
  Dialog.warning({
    title: t("core.user.operations.delete_in_batch.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
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
      await refetch();
      selectedUserNames.value.length = 0;
      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

watch(selectedUserNames, (newValue) => {
  checkedAll.value = newValue.length === users.value?.length;
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
      users.value?.map((user) => {
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

const handleOpenPasswordChangeModal = (user: User) => {
  selectedUser.value = user;
  passwordChangeModal.value = true;
};

const handleOpenGrantPermissionModal = (user: User) => {
  selectedUser.value = user;
  grantPermissionModal.value = true;
};

// Route query action
const routeQueryAction = useRouteQuery<string | undefined>("action");

onMounted(() => {
  if (routeQueryAction.value === "create") {
    creationModal.value = true;
  }
});
</script>
<template>
  <UserEditingModal v-model:visible="editingModal" :user="selectedUser" />

  <UserCreationModal
    v-model:visible="creationModal"
    @close="routeQueryAction = undefined"
  />

  <UserPasswordChangeModal
    v-model:visible="passwordChangeModal"
    :user="selectedUser"
    @close="refetch"
  />

  <GrantPermissionModal
    v-model:visible="grantPermissionModal"
    :user="selectedUser"
    @close="refetch"
  />

  <VPageHeader :title="$t('core.user.title')">
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
          {{ $t("core.user.actions.roles") }}
        </VButton>
        <VButton :route="{ name: 'AuthProviders' }" size="sm" type="default">
          <template #icon>
            <IconLockPasswordLine class="h-full w-full" />
          </template>
          {{ $t("core.user.actions.identity_authentication") }}
        </VButton>
        <VButton
          v-permission="['system:users:manage']"
          type="secondary"
          @click="creationModal = true"
        >
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.new") }}
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
                  :placeholder="$t('core.common.placeholder.search')"
                  type="text"
                  @keyup.enter="handleKeywordChange"
                ></FormKit>

                <FilterTag v-if="keyword" @close="handleClearKeyword()">
                  {{
                    $t("core.common.filters.results.keyword", {
                      keyword: keyword,
                    })
                  }}
                </FilterTag>

                <FilterTag v-if="selectedRole" @close="handleRoleChange()">
                  {{
                    $t("core.user.filters.role.result", {
                      role:
                        selectedRole.metadata.annotations?.[
                          rbacAnnotations.DISPLAY_NAME
                        ] || selectedRole.metadata.name,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedSortItem"
                  @close="handleSortItemChange()"
                >
                  {{
                    $t("core.common.filters.results.sort", {
                      sort: selectedSortItem.label,
                    })
                  }}
                </FilterTag>

                <FilterCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
              </div>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  {{ $t("core.common.buttons.delete") }}
                </VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.user.filters.role.label") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(role, index) in roles"
                      :key="index"
                      :selected="
                        selectedRole?.metadata.name === role.metadata.name
                      "
                      @click="handleRoleChange(role)"
                    >
                      {{
                        role.metadata.annotations?.[
                          rbacAnnotations.DISPLAY_NAME
                        ] || role.metadata.name
                      }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.common.filters.labels.sort") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(sortItem, index) in SortItems"
                      :key="index"
                      :selected="selectedSortItem?.value === sortItem.value"
                      @click="handleSortItemChange(sortItem)"
                    >
                      {{ sortItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="refetch()"
                  >
                    <IconRefreshLine
                      v-tooltip="$t('core.common.buttons.refresh')"
                      :class="{
                        'animate-spin text-gray-900': isFetching,
                      }"
                      class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                    />
                  </div>
                </div>
              </VSpace>
            </div>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!users?.length" appear name="fade">
        <VEmpty
          :message="$t('core.user.empty.message')"
          :title="$t('core.user.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch()">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton
                v-permission="['system:users:manage']"
                type="secondary"
                @click="editingModal = true"
              >
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                {{ $t("core.common.buttons.new") }}
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
          <li v-for="(user, index) in users" :key="index">
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
                    <VStatusDot
                      v-tooltip="$t('core.common.status.deleting')"
                      state="warning"
                      animate
                    />
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
                <VDropdownItem @click="handleOpenCreateModal(user.user)">
                  {{ $t("core.user.operations.update_profile.title") }}
                </VDropdownItem>
                <VDropdownItem
                  @click="handleOpenPasswordChangeModal(user.user)"
                >
                  {{ $t("core.user.operations.change_password.title") }}
                </VDropdownItem>
                <VDropdownItem
                  v-if="
                    userStore.currentUser?.metadata.name !==
                    user.user.metadata.name
                  "
                  @click="handleOpenGrantPermissionModal(user.user)"
                >
                  {{ $t("core.user.operations.grant_permission.title") }}
                </VDropdownItem>
                <VDropdownItem
                  v-if="
                    userStore.currentUser?.metadata.name !==
                    user.user.metadata.name
                  "
                  type="danger"
                  @click="handleDelete(user.user)"
                >
                  {{ $t("core.common.buttons.delete") }}
                </VDropdownItem>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            v-model:page="page"
            v-model:size="size"
            :total="total"
            :page-label="$t('core.components.pagination.page_label')"
            :size-label="$t('core.components.pagination.size_label')"
            :size-options="[20, 30, 50, 100]"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
