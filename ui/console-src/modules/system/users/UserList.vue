<script lang="ts" setup>
import {
  Dialog,
  IconAddCircle,
  IconLockPasswordLine,
  IconRefreshLine,
  IconUserFollow,
  IconUserSettings,
  Toast,
  VAvatar,
  VButton,
  VCard,
  VDropdownItem,
  VEmpty,
  VEntity,
  VEntityField,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import UserEditingModal from "./components/UserEditingModal.vue";
import UserPasswordChangeModal from "./components/UserPasswordChangeModal.vue";
import GrantPermissionModal from "./components/GrantPermissionModal.vue";
import { computed, onMounted, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import type { ListedUser, User } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { formatDatetime } from "@/utils/date";
import { useRouteQuery } from "@vueuse/router";
import { usePermission } from "@/utils/permission";
import { useUserStore } from "@/stores/user";
import { useFetchRole } from "@/composables/use-role";
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
const keyword = useRouteQuery<string>("keyword", "");

const userStore = useUserStore();

const ANONYMOUSUSER_NAME = "anonymousUser";
const DELETEDUSER_NAME = "ghost";

// Filters
const { roles } = useFetchRole();
const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
const selectedRoleValue = useRouteQuery<string | undefined>("role");
const selectedSortValue = useRouteQuery<string | undefined>("sort");

function handleClearFilters() {
  selectedRoleValue.value = undefined;
  selectedSortValue.value = undefined;
}

const hasFilters = computed(() => {
  return selectedRoleValue.value || selectedSortValue.value;
});

watch(
  () => [selectedRoleValue.value, selectedSortValue.value, keyword.value],
  () => {
    page.value = 1;
  }
);

const total = ref(0);

const {
  data: users,
  isLoading,
  isFetching,
  refetch,
} = useQuery<ListedUser[]>({
  queryKey: [
    "users",
    page,
    size,
    keyword,
    selectedSortValue,
    selectedRoleValue,
  ],
  queryFn: async () => {
    const { data } = await apiClient.user.listUsers({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      fieldSelector: [
        `name!=${ANONYMOUSUSER_NAME}`,
        `name!=${DELETEDUSER_NAME}`,
      ],
      sort: [selectedSortValue.value].filter(Boolean) as string[],
      role: selectedRoleValue.value,
    });

    total.value = data.total;

    return data.items;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.some(
      (user) => !!user.user.metadata.deletionTimestamp
    );

    return hasDeletingData ? 1000 : false;
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
        await apiClient.extension.user.deleteV1alpha1User({
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
          return apiClient.extension.user.deleteV1alpha1User({
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
  checkedAll.value =
    newValue.length ===
    users.value?.filter(
      (user) => user.user.metadata.name !== userStore.currentUser?.metadata.name
    ).length;
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
      users.value
        ?.filter((user) => {
          return (
            user.user.metadata.name !== userStore.currentUser?.metadata.name
          );
        })
        .map((user) => {
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

function onCreationModalClose() {
  creationModal.value = false;
  routeQueryAction.value = undefined;
}

function onEditingModalClose() {
  editingModal.value = false;
  selectedUser.value = undefined;
}

function onPasswordChangeModalClose() {
  passwordChangeModal.value = false;
  refetch();
}

function onGrantPermissionModalClose() {
  grantPermissionModal.value = false;
  selectedUser.value = undefined;
  refetch();
}
</script>
<template>
  <UserEditingModal
    v-if="editingModal && selectedUser"
    :user="selectedUser"
    @close="onEditingModalClose"
  />

  <UserCreationModal v-if="creationModal" @close="onCreationModalClose" />

  <UserPasswordChangeModal
    v-if="passwordChangeModal"
    :user="selectedUser"
    @close="onPasswordChangeModalClose"
  />

  <GrantPermissionModal
    v-if="grantPermissionModal"
    :user="selectedUser"
    @close="onGrantPermissionModalClose"
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
            class="relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <div
              v-permission="['system:users:manage']"
              class="hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <SearchInput v-if="!selectedUserNames.length" v-model="keyword" />
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  {{ $t("core.common.buttons.delete") }}
                </VButton>
              </VSpace>
            </div>
            <VSpace spacing="lg" class="flex-wrap">
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
              />
              <FilterDropdown
                v-model="selectedRoleValue"
                :label="$t('core.user.filters.role.label')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.all'),
                  },
                  ...roles.map((role) => {
                    return {
                      label:
                        role.metadata.annotations?.[
                          rbacAnnotations.DISPLAY_NAME
                        ] || role.metadata.name,
                      value: role.metadata.name,
                    };
                  }),
                ]"
              />
              <FilterDropdown
                v-model="selectedSortValue"
                :label="$t('core.common.filters.labels.sort')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.default'),
                  },
                  {
                    label: t('core.user.filters.sort.items.create_time_desc'),
                    value: 'metadata.creationTimestamp,desc',
                  },
                  {
                    label: t('core.user.filters.sort.items.create_time_asc'),
                    value: 'metadata.creationTimestamp,asc',
                  },
                ]"
              />
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
                  name="post-checkbox"
                  type="checkbox"
                  :disabled="
                    user.user.metadata.name ===
                    userStore.currentUser?.metadata.name
                  "
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
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :total="total"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', { total: total })
          "
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
