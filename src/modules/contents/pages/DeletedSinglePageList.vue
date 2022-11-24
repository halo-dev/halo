<script lang="ts" setup>
import {
  IconAddCircle,
  IconRefreshLine,
  IconDeleteBin,
  VButton,
  VCard,
  VPagination,
  VSpace,
  Dialog,
  VEmpty,
  VAvatar,
  VEntity,
  VEntityField,
  VPageHeader,
  VStatusDot,
  VLoading,
} from "@halo-dev/components";
import { onMounted, ref, watch } from "vue";
import type { ListedSinglePageList, SinglePage } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { onBeforeRouteLeave, RouterLink } from "vue-router";
import cloneDeep from "lodash.clonedeep";
import { usePermission } from "@/utils/permission";
import { getNode } from "@formkit/core";
import FilterTag from "@/components/filter/FilterTag.vue";

const { currentUserHasPermission } = usePermission();

const singlePages = ref<ListedSinglePageList>({
  page: 1,
  size: 50,
  total: 0,
  items: [],
  first: true,
  last: false,
  hasNext: false,
  hasPrevious: false,
  totalPages: 0,
});
const loading = ref(false);
const selectedPageNames = ref<string[]>([]);
const checkedAll = ref(false);
const refreshInterval = ref();
const keyword = ref("");

const handleFetchSinglePages = async (options?: {
  mute?: boolean;
  page?: number;
}) => {
  try {
    clearInterval(refreshInterval.value);

    if (!options?.mute) {
      loading.value = true;
    }

    if (options?.page) {
      singlePages.value.page = options.page;
    }

    const { data } = await apiClient.singlePage.listSinglePages({
      labelSelector: [`content.halo.run/deleted=true`],
      page: singlePages.value.page,
      size: singlePages.value.size,
      keyword: keyword.value,
    });

    singlePages.value = data;

    const deletedSinglePages = singlePages.value.items.filter(
      (singlePage) =>
        !!singlePage.page.metadata.deletionTimestamp ||
        !singlePage.page.spec.deleted
    );

    if (deletedSinglePages.length) {
      refreshInterval.value = setInterval(() => {
        handleFetchSinglePages({ mute: true });
      }, 3000);
    }
  } catch (error) {
    console.error("Failed to fetch deleted single pages", error);
  } finally {
    loading.value = false;
  }
};

onBeforeRouteLeave(() => {
  clearInterval(refreshInterval.value);
});

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

const checkSelection = (singlePage: SinglePage) => {
  return selectedPageNames.value.includes(singlePage.metadata.name);
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedPageNames.value =
      singlePages.value.items.map((singlePage) => {
        return singlePage.page.metadata.name;
      }) || [];
  } else {
    selectedPageNames.value = [];
  }
};

const handleDeletePermanently = async (singlePage: SinglePage) => {
  Dialog.warning({
    title: "是否确认永久删除该自定义页面？",
    description: "删除之后将无法恢复",
    confirmType: "danger",
    onConfirm: async () => {
      await apiClient.extension.singlePage.deletecontentHaloRunV1alpha1SinglePage(
        {
          name: singlePage.metadata.name,
        }
      );
      await handleFetchSinglePages();
    },
  });
};

const handleDeletePermanentlyInBatch = async () => {
  Dialog.warning({
    title: "是否确认永久删除选中的自定义页面？",
    description: "删除之后将无法恢复",
    confirmType: "danger",
    onConfirm: async () => {
      await Promise.all(
        selectedPageNames.value.map((name) => {
          return apiClient.extension.singlePage.deletecontentHaloRunV1alpha1SinglePage(
            {
              name,
            }
          );
        })
      );
      await handleFetchSinglePages();
      selectedPageNames.value = [];
    },
  });
};

const handleRecovery = async (singlePage: SinglePage) => {
  Dialog.warning({
    title: "是否确认恢复该自定义页面？",
    description: "此操作会将自定义页面恢复到被删除之前的状态",
    onConfirm: async () => {
      const singlePageToUpdate = cloneDeep(singlePage);
      singlePageToUpdate.spec.deleted = false;
      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: singlePageToUpdate.metadata.name,
          singlePage: singlePageToUpdate,
        }
      );
      await handleFetchSinglePages();
    },
  });
};

const handleRecoveryInBatch = async () => {
  Dialog.warning({
    title: "是否确认恢复选中的自定义页面？",
    description: "此操作会将自定义页面恢复到被删除之前的状态",
    onConfirm: async () => {
      await Promise.all(
        selectedPageNames.value.map((name) => {
          const singlePage = singlePages.value.items.find(
            (item) => item.page.metadata.name === name
          )?.page;

          if (!singlePage) {
            return Promise.resolve();
          }

          singlePage.spec.deleted = false;
          return apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
            {
              name: singlePage.metadata.name,
              singlePage: singlePage,
            }
          );
        })
      );
      await handleFetchSinglePages();
      selectedPageNames.value = [];
    },
  });
};

watch(selectedPageNames, (newValue) => {
  checkedAll.value = newValue.length === singlePages.value.items?.length;
});

onMounted(handleFetchSinglePages);

// Filters
function handleKeywordChange() {
  const keywordNode = getNode("keywordInput");
  if (keywordNode) {
    keyword.value = keywordNode._value as string;
  }
  handleFetchSinglePages({ page: 1 });
}

function handleClearKeyword() {
  keyword.value = "";
  handleFetchSinglePages({ page: 1 });
}
</script>

<template>
  <VPageHeader title="自定义页面回收站">
    <template #icon>
      <IconDeleteBin class="mr-2 self-center text-green-600" />
    </template>
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'SinglePages' }" size="sm">返回</VButton>
        <VButton
          v-permission="['system:singlepages:manage']"
          :route="{ name: 'SinglePageEditor' }"
          type="secondary"
        >
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          新建
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
              v-permission="['system:singlepages:manage']"
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
                v-if="!selectedPageNames.length"
                class="flex items-center gap-2"
              >
                <FormKit
                  id="keywordInput"
                  outer-class="!p-0"
                  placeholder="输入关键词搜索"
                  type="text"
                  name="keyword"
                  :model-value="keyword"
                  @keyup.enter="handleKeywordChange"
                ></FormKit>

                <FilterTag v-if="keyword" @close="handleClearKeyword()">
                  关键词：{{ keyword }}
                </FilterTag>
              </div>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeletePermanentlyInBatch">
                  永久删除
                </VButton>
                <VButton type="default" @click="handleRecoveryInBatch">
                  恢复
                </VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="handleFetchSinglePages()"
                  >
                    <IconRefreshLine
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
      <Transition v-else-if="!singlePages.items.length" appear name="fade">
        <VEmpty
          message="你可以尝试刷新或者返回自定义页面管理"
          title="没有自定义页面被放入回收站"
        >
          <template #actions>
            <VSpace>
              <VButton @click="handleFetchSinglePages">刷新</VButton>
              <VButton
                v-permission="['system:singlepages:view']"
                :route="{ name: 'SinglePages' }"
                type="primary"
              >
                返回
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
          <li v-for="(singlePage, index) in singlePages.items" :key="index">
            <VEntity :is-selected="checkSelection(singlePage.page)">
              <template
                v-if="currentUserHasPermission(['system:singlepages:manage'])"
                #checkbox
              >
                <input
                  v-model="selectedPageNames"
                  :value="singlePage.page.metadata.name"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField :title="singlePage.page.spec.title">
                  <template #description>
                    <VSpace>
                      <span class="text-xs text-gray-500">
                        {{ singlePage.page.status?.permalink }}
                      </span>
                      <span class="text-xs text-gray-500">
                        访问量 {{ singlePage.stats.visit || 0 }}
                      </span>
                      <span class="text-xs text-gray-500">
                        评论 {{ singlePage.stats.totalComment || 0 }}
                      </span>
                    </VSpace>
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
                <VEntityField v-if="!singlePage?.page?.spec.deleted">
                  <template #description>
                    <VStatusDot v-tooltip="`恢复中`" state="success" animate />
                  </template>
                </VEntityField>
                <VEntityField
                  v-if="singlePage?.page?.metadata.deletionTimestamp"
                >
                  <template #description>
                    <VStatusDot v-tooltip="`删除中`" state="warning" animate />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class="truncate text-xs tabular-nums text-gray-500">
                      {{ formatDatetime(singlePage.page.spec.publishTime) }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template
                v-if="currentUserHasPermission(['system:singlepages:manage'])"
                #dropdownItems
              >
                <VButton
                  v-close-popper
                  block
                  type="danger"
                  @click="handleDeletePermanently(singlePage.page)"
                >
                  永久删除
                </VButton>
                <VButton
                  v-close-popper
                  block
                  type="default"
                  @click="handleRecovery(singlePage.page)"
                >
                  恢复
                </VButton>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            :page="singlePages.page"
            :size="singlePages.size"
            :total="singlePages.total"
            :size-options="[20, 30, 50, 100]"
            @change="handlePaginationChange"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
