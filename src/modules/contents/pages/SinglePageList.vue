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
  VStatusDot,
  VEntity,
  VEntityField,
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
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { RouterLink } from "vue-router";
import cloneDeep from "lodash.clonedeep";

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

    let contributors: string[] | undefined;

    if (selectedContributor.value) {
      contributors = [selectedContributor.value.metadata.name];
    }

    const { data } = await apiClient.singlePage.listSinglePages({
      page: singlePages.value.page,
      size: singlePages.value.size,
      visible: selectedVisibleItem.value.value,
      sort: selectedSortItem.value?.sort,
      publishPhase: selectedPublishPhaseItem.value.value,
      sortOrder: selectedSortItem.value?.sortOrder,
      keyword: keyword.value,
      contributor: contributors,
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

interface VisibleItem {
  label: string;
  value?: "PUBLIC" | "INTERNAL" | "PRIVATE";
}

interface PublishPhaseItem {
  label: string;
  value?: "DRAFT" | "PENDING_APPROVAL" | "PUBLISHED";
}

interface SortItem {
  label: string;
  sort: "PUBLISH_TIME" | "CREATE_TIME";
  sortOrder: boolean;
}

const VisibleItems: VisibleItem[] = [
  {
    label: "全部",
    value: undefined,
  },
  {
    label: "公开",
    value: "PUBLIC",
  },
  {
    label: "内部成员可访问",
    value: "INTERNAL",
  },
  {
    label: "私有",
    value: "PRIVATE",
  },
];

const PublishPhaseItems: PublishPhaseItem[] = [
  {
    label: "全部",
    value: undefined,
  },
  {
    label: "已发布",
    value: "PUBLISHED",
  },
  {
    label: "未发布",
    value: "DRAFT",
  },
  {
    label: "待审核",
    value: "PENDING_APPROVAL",
  },
];

const SortItems: SortItem[] = [
  {
    label: "较近发布",
    sort: "PUBLISH_TIME",
    sortOrder: false,
  },
  {
    label: "较早发布",
    sort: "PUBLISH_TIME",
    sortOrder: true,
  },
  {
    label: "较近创建",
    sort: "CREATE_TIME",
    sortOrder: false,
  },
  {
    label: "较早创建",
    sort: "CREATE_TIME",
    sortOrder: true,
  },
];

const selectedContributor = ref<User>();
const selectedVisibleItem = ref<VisibleItem>(VisibleItems[0]);
const selectedPublishPhaseItem = ref<PublishPhaseItem>(PublishPhaseItems[0]);
const selectedSortItem = ref<SortItem>();
const keyword = ref("");

function handleVisibleItemChange(visibleItem: VisibleItem) {
  selectedVisibleItem.value = visibleItem;
  handleFetchSinglePages();
}

const handleSelectUser = (user?: User) => {
  selectedContributor.value = user;
  handleFetchSinglePages();
};

function handlePublishPhaseItemChange(publishPhaseItem: PublishPhaseItem) {
  selectedPublishPhaseItem.value = publishPhaseItem;
  handleFetchSinglePages();
}

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  handleFetchSinglePages();
}
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
          <div class="flex w-full flex-1 items-center sm:w-auto">
            <div v-if="!checkAll" class="flex items-center gap-2">
              <FormKit
                v-model="keyword"
                placeholder="输入关键词搜索"
                type="text"
                @keyup.enter="handleFetchSinglePages"
              ></FormKit>
              <div
                v-if="selectedPublishPhaseItem.value"
                class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
              >
                <span class="text-xs text-gray-600 group-hover:text-gray-900">
                  状态：{{ selectedPublishPhaseItem.label }}
                </span>
                <IconCloseCircle
                  class="h-4 w-4 text-gray-600"
                  @click="handlePublishPhaseItemChange(PublishPhaseItems[0])"
                />
              </div>
              <div
                v-if="selectedVisibleItem.value"
                class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
              >
                <span class="text-xs text-gray-600 group-hover:text-gray-900">
                  可见性：{{ selectedVisibleItem.label }}
                </span>
                <IconCloseCircle
                  class="h-4 w-4 text-gray-600"
                  @click="handleVisibleItemChange(VisibleItems[0])"
                />
              </div>
              <div
                v-if="selectedContributor"
                class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
              >
                <span class="text-xs text-gray-600 group-hover:text-gray-900">
                  作者：{{ selectedContributor?.spec.displayName }}
                </span>
                <IconCloseCircle
                  class="h-4 w-4 text-gray-600"
                  @click="handleSelectUser(undefined)"
                />
              </div>
              <div
                v-if="selectedSortItem"
                class="group flex cursor-pointer items-center justify-center gap-1 rounded-full bg-gray-200 px-2 py-1 hover:bg-gray-300"
              >
                <span class="text-xs text-gray-600 group-hover:text-gray-900">
                  排序：{{ selectedSortItem.label }}
                </span>
                <IconCloseCircle
                  class="h-4 w-4 text-gray-600"
                  @click="handleSortItemChange()"
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
                  <div class="w-72 p-4">
                    <ul class="space-y-1">
                      <li
                        v-for="(filterItem, index) in PublishPhaseItems"
                        :key="index"
                        v-close-popper
                        :class="{
                          'bg-gray-100':
                            selectedPublishPhaseItem.value === filterItem.value,
                        }"
                        class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        @click="handlePublishPhaseItemChange(filterItem)"
                      >
                        <span class="truncate">{{ filterItem.label }}</span>
                      </li>
                    </ul>
                  </div>
                </template>
              </FloatingDropdown>
              <FloatingDropdown>
                <div
                  class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                >
                  <span class="mr-0.5"> 可见性 </span>
                  <span>
                    <IconArrowDown />
                  </span>
                </div>
                <template #popper>
                  <div class="w-72 p-4">
                    <ul class="space-y-1">
                      <li
                        v-for="(filterItem, index) in VisibleItems"
                        :key="index"
                        v-close-popper
                        :class="{
                          'bg-gray-100':
                            selectedVisibleItem.value === filterItem.value,
                        }"
                        class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                        @click="handleVisibleItemChange(filterItem)"
                      >
                        <span class="truncate">
                          {{ filterItem.label }}
                        </span>
                      </li>
                    </ul>
                  </div>
                </template>
              </FloatingDropdown>
              <UserDropdownSelector
                v-model:selected="selectedContributor"
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
        <VEntity :is-selected="checkAll">
          <template #checkbox>
            <input
              v-model="checkAll"
              class="h-4 w-4 rounded border-gray-300 text-indigo-600"
              type="checkbox"
            />
          </template>
          <template #start>
            <VEntityField
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
                  <VStatusDot state="success" animate />
                </RouterLink>
              </template>
            </VEntityField>
          </template>
          <template #end>
            <VEntityField>
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
            </VEntityField>
            <VEntityField :description="finalStatus(singlePage.page)" />
            <VEntityField>
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
            </VEntityField>
            <VEntityField>
              <template #description>
                <span class="truncate text-xs tabular-nums text-gray-500">
                  {{
                    formatDatetime(singlePage.page.metadata.creationTimestamp)
                  }}
                </span>
              </template>
            </VEntityField>
          </template>
          <template #dropdownItems>
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
        </VEntity>
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
