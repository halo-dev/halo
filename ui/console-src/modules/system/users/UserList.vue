<script lang="ts" setup>
import { useFetchRole } from "@/composables/use-role";
import { rbacAnnotations } from "@/constants/annotations";
import { useUserStore } from "@/stores/user";
import { usePermission } from "@/utils/permission";
import type { ListedUser, User } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconLockPasswordLine,
  IconRefreshLine,
  IconShieldUser,
  IconUserSettings,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VEntityContainer,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { chunk } from "lodash-es";
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import UserCreationModal from "./components/UserCreationModal.vue";
import UserListItem from "./components/UserListItem.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const checkedAll = ref(false);
const creationModal = ref<boolean>(false);

const selectedUserNames = ref<string[]>([]);
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
    const { data } = await consoleApiClient.user.listUsers({
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
});

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
          return coreApiClient.user.deleteUser({
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

function handleEnableOrDisableInBatch(operation: "enable" | "disable") {
  const operations = {
    enable: {
      title: t("core.user.operations.enable_in_batch.title"),
      description: t("core.user.operations.enable_in_batch.description"),
      request: (name: string) =>
        consoleApiClient.user.enableUser({ username: name }),
      condition: (user: User) => user.spec.disabled,
      message: t("core.common.toast.enable_success"),
    },
    disable: {
      title: t("core.user.operations.disable_in_batch.title"),
      description: t("core.user.operations.disable_in_batch.description"),
      request: (name: string) =>
        consoleApiClient.user.disableUser({ username: name }),
      condition: (user: User) => !user.spec.disabled,
      message: t("core.common.toast.disable_success"),
    },
  };

  const operationConfig = operations[operation];

  Dialog.warning({
    title: operationConfig.title,
    description: operationConfig.description,
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      const filteredUserNames = selectedUserNames.value.filter((name) => {
        if (name === userStore.currentUser?.metadata.name) return false;
        const user = users.value?.find((u) => u.user.metadata.name === name);
        return user && operationConfig.condition(user.user);
      });

      const chunks = chunk(filteredUserNames, 5);

      for (const chunk of chunks) {
        await Promise.all(chunk.map((name) => operationConfig.request(name)));
      }

      await refetch();

      selectedUserNames.value.length = 0;
      checkedAll.value = false;

      Toast.success(operationConfig.message);
    },
  });
}

watch(selectedUserNames, (newValue) => {
  checkedAll.value =
    newValue.length ===
    users.value?.filter(
      (user) => user.user.metadata.name !== userStore.currentUser?.metadata.name
    ).length;
});

const checkSelection = (user: User) => {
  return selectedUserNames.value.includes(user.metadata.name);
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
</script>
<template>
  <UserCreationModal v-if="creationModal" @close="onCreationModalClose" />

  <VPageHeader :title="$t('core.user.title')">
    <template #icon>
      <IconUserSettings />
    </template>
    <template #actions>
      <VButton
        v-permission="['system:roles:view']"
        :route="{ name: 'Roles' }"
        size="sm"
        type="default"
      >
        <template #icon>
          <IconShieldUser />
        </template>
        {{ $t("core.user.actions.roles") }}
      </VButton>
      <HasPermission :permissions="['*']">
        <VButton :route="{ name: 'AuthProviders' }" size="sm" type="default">
          <template #icon>
            <IconLockPasswordLine />
          </template>
          {{ $t("core.user.actions.identity_authentication") }}
        </VButton>
      </HasPermission>
      <VButton
        v-permission="['system:users:manage']"
        type="secondary"
        @click="creationModal = true"
      >
        <template #icon>
          <IconAddCircle />
        </template>
        {{ $t("core.common.buttons.new") }}
      </VButton>
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
                <VButton @click="handleEnableOrDisableInBatch('disable')">
                  {{ $t("core.common.buttons.disable") }}
                </VButton>
                <VButton @click="handleEnableOrDisableInBatch('enable')">
                  {{ $t("core.common.buttons.enable") }}
                </VButton>
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
                @click="creationModal = true"
              >
                <template #icon>
                  <IconAddCircle />
                </template>
                {{ $t("core.common.buttons.new") }}
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <VEntityContainer>
          <UserListItem
            v-for="user in users"
            :key="user.user.metadata.name"
            :user="user"
            :is-selected="checkSelection(user.user)"
          >
            <template
              v-if="currentUserHasPermission(['system:users:manage'])"
              #checkbox
            >
              <input
                v-model="selectedUserNames"
                :value="user.user.metadata.name"
                name="user-checkbox"
                type="checkbox"
                :disabled="
                  user.user.metadata.name ===
                  userStore.currentUser?.metadata.name
                "
              />
            </template>
          </UserListItem>
        </VEntityContainer>
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
