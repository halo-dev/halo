<script lang="ts" setup>
import {
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconEye,
  IconEyeOff,
  IconTeam,
  IconCloseCircle,
  IconAddCircle,
  VButton,
  VCard,
  VPagination,
  VSpace,
  useDialog,
  VEmpty,
  VAvatar,
} from "@halo-dev/components";
import SinglePageSettingModal from "./components/SinglePageSettingModal.vue";
import UserDropdownSelector from "@/components/dropdown-selector/UserDropdownSelector.vue";
import { onMounted, ref, watchEffect } from "vue";
import type {
  ListedSinglePageList,
  SinglePage,
  SinglePageRequest,
  User,
} from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";
import { formatDatetime } from "@/utils/date";
import { RouterLink } from "vue-router";
import cloneDeep from "lodash.clonedeep";
import Entity from "../../../components/entity/Entity.vue";
import EntityField from "../../../components/entity/EntityField.vue";

enum SinglePagePhase {
  DRAFT = "未发布",
  PENDING_APPROVAL = "待审核",
  PUBLISHED = "已发布",
}

const dialog = useDialog();

const singlePages = ref<ListedSinglePageList>({
  page: 1,
  size: 20,
  total: 0,
  items: [],
  first: true,
  last: false,
  hasNext: false,
  hasPrevious: false,
});
const loading = ref(false);
const settingModal = ref(false);
const selectedSinglePage = ref<SinglePage>();
const selectedSinglePageWithContent = ref<SinglePageRequest>();
const checkAll = ref(false);

const handleFetchSinglePages = async () => {
  try {
    loading.value = true;
    const { data } = await apiClient.singlePage.listSinglePages({
      page: singlePages.value.page,
      size: singlePages.value.size,
    });
    singlePages.value = data;
  } catch (error) {
    console.error("Failed to fetch single pages", error);
  } finally {
    loading.value = false;
  }
};

const handlePaginationChange = ({
  page,
  size,
}: {
  page: number;
  size: number;
}) => {
  singlePages.value.page = page;
  singlePages.value.size = size;
  handleFetchSinglePages();
};

const handleOpenSettingModal = async (singlePage: SinglePage) => {
  const { data } =
    await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
      name: singlePage.metadata.name,
    });
  selectedSinglePage.value = data;
  settingModal.value = true;
};

const onSettingModalClose = () => {
  selectedSinglePage.value = undefined;
  selectedSinglePageWithContent.value = undefined;
  handleFetchSinglePages();
};

watchEffect(async () => {
  if (
    !selectedSinglePage.value ||
    !selectedSinglePage.value.spec.headSnapshot
  ) {
    return;
  }

  const { data: content } = await apiClient.content.obtainSnapshotContent({
    snapshotName: selectedSinglePage.value.spec.headSnapshot,
  });

  selectedSinglePageWithContent.value = {
    page: selectedSinglePage.value,
    content: content,
  };
});

const handleSelectPrevious = async () => {
  const { items, hasPrevious } = singlePages.value;
  const index = items.findIndex(
    (singlePage) =>
      singlePage.page.metadata.name === selectedSinglePage.value?.metadata.name
  );
  if (index > 0) {
    const { data } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: items[index - 1].page.metadata.name,
      });
    selectedSinglePage.value = data;
    return;
  }
  if (index === 0 && hasPrevious) {
    singlePages.value.page--;
    await handleFetchSinglePages();
    selectedSinglePage.value =
      singlePages.value.items[singlePages.value.items.length - 1].page;
  }
};

const handleSelectNext = async () => {
  const { items, hasNext } = singlePages.value;
  const index = items.findIndex(
    (singlePage) =>
      singlePage.page.metadata.name === selectedSinglePage.value?.metadata.name
  );
  if (index < items.length - 1) {
    const { data } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: items[index + 1].page.metadata.name,
      });
    selectedSinglePage.value = data;
    return;
  }
  if (index === items.length - 1 && hasNext) {
    singlePages.value.page++;
    await handleFetchSinglePages();
    selectedSinglePage.value = singlePages.value.items[0].page;
  }
};

const handleDelete = async (singlePage: SinglePage) => {
  dialog.warning({
    title: "是否确认删除该自定义页面？",
    confirmType: "danger",
    onConfirm: async () => {
      const singlePageToUpdate = cloneDeep(singlePage);
      singlePageToUpdate.spec.deleted = true;
      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: singlePage.metadata.name,
          singlePage: singlePageToUpdate,
        }
      );
      await handleFetchSinglePages();
    },
  });
};

const finalStatus = (singlePage: SinglePage) => {
  if (singlePage.status?.phase) {
    return SinglePagePhase[singlePage.status.phase];
  }
  return "";
};

onMounted(handleFetchSinglePages);

// Filters
const selectedUser = ref<User>();

const handleSelectUser = (user?: User) => {
  selectedUser.value = user;
  handleFetchSinglePages();
};
</script>

<template>
  <SinglePageSettingModal
    v-model:visible="settingModal"
    :single-page="selectedSinglePageWithContent"
    @close="onSettingModalClose"
  >
    <template #actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
    </template>
  </SinglePageSettingModal>
  <VCard :body-class="['!p-0']" class="rounded-none border-none shadow-none">
    <template #header>
      <div class="block w-full bg-gray-50 px-4 py-3">
        <div
          class="relative flex flex-col items-start sm:flex-row sm:items-center"
        >
          <div class="mr-4 hidden items-center sm:flex">
            <input
              v-model="checkAll"
              class="h-4 w-4 rounded border-gray-300 text-indigo-600"
              type="checkbox"
            />
          </div>
          <div class="flex w-full flex-1 sm:w-auto">
            <div v-if="!checkAll" class="flex items-center gap-2">
              <FormKit placeholder="输入关键词搜索" type="text"></FormKit>
              <div
                v-if="selectedUser"
                class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
              >
                <span class="text-xs text-gray-600 group-hover:text-gray-900">
                  作者：{{ selectedUser?.spec.displayName }}
                </span>
                <IconCloseCircle
                  class="h-4 w-4 text-gray-600"
                  @click="handleSelectUser(undefined)"
                />
              </div>
            </div>
            <VSpace v-else>
              <VButton type="default">设置</VButton>
              <VButton type="danger">删除</VButton>
            </VSpace>
          </div>
          <div class="mt-4 flex sm:mt-0">
            <VSpace spacing="lg">
              <UserDropdownSelector
                v-model:selected="selectedUser"
                @select="handleSelectUser"
              >
                <div
                  class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                >
                  <span class="mr-0.5">作者</span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
              </UserDropdownSelector>
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
                        <span class="truncate">较近发布</span>
                      </li>
                      <li
                        class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                      >
                        <span class="truncate">较晚发布</span>
                      </li>
                      <li
                        class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                      >
                        <span class="truncate">浏览量最多</span>
                      </li>
                      <li
                        class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                      >
                        <span class="truncate">浏览量最少</span>
                      </li>
                      <li
                        class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                      >
                        <span class="truncate">评论量最多</span>
                      </li>
                      <li
                        class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                      >
                        <span class="truncate">评论量最少</span>
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
    <VEmpty
      v-if="!singlePages.items.length && !loading"
      message="你可以尝试刷新或者新建页面"
      title="当前没有页面"
    >
      <template #actions>
        <VSpace>
          <VButton @click="handleFetchSinglePages">刷新</VButton>
          <VButton :route="{ name: 'SinglePageEditor' }" type="primary">
            <template #icon>
              <IconAddCircle class="h-full w-full" />
            </template>
            新建页面
          </VButton>
        </VSpace>
      </template>
    </VEmpty>
    <ul
      v-else
      class="box-border h-full w-full divide-y divide-gray-100"
      role="list"
    >
      <li v-for="(singlePage, index) in singlePages.items" :key="index">
        <Entity :is-selected="checkAll">
          <template #checkbox>
            <input
              v-model="checkAll"
              class="h-4 w-4 rounded border-gray-300 text-indigo-600"
              type="checkbox"
            />
          </template>
          <template #start>
            <EntityField
              :title="singlePage.page.spec.title"
              :description="singlePage.page.status?.permalink"
              :route="{
                name: 'SinglePageEditor',
                query: { name: singlePage.page.metadata.name },
              }"
            >
              <template #extra>
                <RouterLink
                  v-if="singlePage.page.status?.inProgress"
                  v-tooltip="`当前有内容已保存，但还未发布。`"
                  :to="{
                    name: 'SinglePageEditor',
                    query: { name: singlePage.page.metadata.name },
                  }"
                  class="flex items-center"
                >
                  <div
                    class="inline-flex h-1.5 w-1.5 rounded-full bg-orange-600"
                  >
                    <span
                      class="inline-block h-1.5 w-1.5 animate-ping rounded-full bg-orange-600"
                    ></span>
                  </div>
                </RouterLink>
              </template>
            </EntityField>
          </template>
          <template #end>
            <EntityField>
              <template #description>
                <RouterLink
                  v-for="(
                    contributor, contributorIndex
                  ) in singlePage.contributors"
                  :key="contributorIndex"
                  :to="{
                    name: 'UserDetail',
                    params: { name: contributor.name },
                  }"
                  class="flex items-center"
                >
                  <VAvatar
                    v-tooltip="contributor.displayName"
                    size="xs"
                    :src="contributor.avatar"
                    :alt="contributor.displayName"
                    circle
                  ></VAvatar>
                </RouterLink>
              </template>
            </EntityField>
            <EntityField :description="finalStatus(singlePage.page)" />
            <EntityField>
              <template #description>
                <IconEye
                  v-if="singlePage.page.spec.visible === 'PUBLIC'"
                  v-tooltip="`公开访问`"
                  class="cursor-pointer text-sm transition-all hover:text-blue-600"
                />
                <IconEyeOff
                  v-if="singlePage.page.spec.visible === 'PRIVATE'"
                  v-tooltip="`私有访问`"
                  class="cursor-pointer text-sm transition-all hover:text-blue-600"
                />
                <IconTeam
                  v-if="singlePage.page.spec.visible === 'INTERNAL'"
                  v-tooltip="`内部成员可访问`"
                  class="cursor-pointer text-sm transition-all hover:text-blue-600"
                />
              </template>
            </EntityField>
            <EntityField
              :description="
                formatDatetime(singlePage.page.metadata.creationTimestamp)
              "
            />
          </template>
          <template #menuItems>
            <VButton
              v-close-popper
              block
              type="secondary"
              @click="handleOpenSettingModal(singlePage.page)"
            >
              设置
            </VButton>
            <VButton
              v-close-popper
              block
              type="danger"
              @click="handleDelete(singlePage.page)"
            >
              删除
            </VButton>
          </template>
        </Entity>
      </li>
    </ul>

    <template #footer>
      <div class="bg-white sm:flex sm:items-center sm:justify-end">
        <VPagination
          :page="singlePages.page"
          :size="singlePages.size"
          :total="singlePages.total"
          @change="handlePaginationChange"
        />
      </div>
    </template>
  </VCard>
</template>
