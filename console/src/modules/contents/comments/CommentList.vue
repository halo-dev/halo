<script lang="ts" setup>
import {
  IconArrowDown,
  IconMessage,
  VButton,
  VCard,
  VPageHeader,
  VPagination,
  VSpace,
  IconRefreshLine,
  VEmpty,
  Dialog,
  VLoading,
  Toast,
} from "@halo-dev/components";
import CommentListItem from "./components/CommentListItem.vue";
import UserDropdownSelector from "@/components/dropdown-selector/UserDropdownSelector.vue";
import type { ListedComment, User } from "@halo-dev/api-client";
import { computed, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import FilterTag from "@/components/filter/FilterTag.vue";
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import { getNode } from "@formkit/core";
import { useQuery } from "@tanstack/vue-query";

const checkAll = ref(false);
const selectedComment = ref<ListedComment>();
const selectedCommentNames = ref<string[]>([]);
const keyword = ref("");

// Filters
const ApprovedFilterItems: { label: string; value?: boolean }[] = [
  {
    label: "全部",
    value: undefined,
  },
  {
    label: "已审核",
    value: true,
  },
  {
    label: "待审核",
    value: false,
  },
];

type Sort = "LAST_REPLY_TIME" | "REPLY_COUNT" | "CREATE_TIME";

const SortFilterItems: {
  label: string;
  value?: Sort;
}[] = [
  {
    label: "默认",
    value: undefined,
  },
  {
    label: "最后回复时间",
    value: "LAST_REPLY_TIME",
  },
  {
    label: "回复数",
    value: "REPLY_COUNT",
  },
  {
    label: "创建时间",
    value: "CREATE_TIME",
  },
];

const selectedApprovedFilterItem = ref<{ label: string; value?: boolean }>(
  ApprovedFilterItems[0]
);

const selectedSortFilterItem = ref<{
  label: string;
  value?: Sort;
}>(SortFilterItems[0]);

const selectedUser = ref<User>();

const handleApprovedFilterItemChange = (filterItem: {
  label: string;
  value?: boolean;
}) => {
  selectedApprovedFilterItem.value = filterItem;
  selectedCommentNames.value = [];
  page.value = 1;
};

const handleSortFilterItemChange = (filterItem: {
  label: string;
  value?: Sort;
}) => {
  selectedSortFilterItem.value = filterItem;
  selectedCommentNames.value = [];
  page.value = 1;
};

function handleSelectUser(user: User | undefined) {
  selectedUser.value = user;
  page.value = 1;
}

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

const hasFilters = computed(() => {
  return (
    selectedApprovedFilterItem.value.value !== undefined ||
    selectedSortFilterItem.value.value !== undefined ||
    selectedUser.value ||
    keyword.value
  );
});

function handleClearFilters() {
  selectedApprovedFilterItem.value = ApprovedFilterItems[0];
  selectedSortFilterItem.value = SortFilterItems[0];
  selectedUser.value = undefined;
  keyword.value = "";
  page.value = 1;
}

const page = ref(1);
const size = ref(20);
const total = ref(0);

const {
  data: comments,
  isLoading,
  isFetching,
  refetch,
} = useQuery<ListedComment[]>({
  queryKey: [
    "comments",
    page,
    size,
    selectedApprovedFilterItem,
    selectedSortFilterItem,
    selectedUser,
    keyword,
  ],
  queryFn: async () => {
    const { data } = await apiClient.comment.listComments({
      page: page.value,
      size: size.value,
      approved: selectedApprovedFilterItem.value.value,
      sort: selectedSortFilterItem.value.value,
      keyword: keyword.value,
      ownerName: selectedUser.value?.metadata.name,
    });

    total.value = data.total;

    return data.items;
  },
  refetchOnWindowFocus: false,
  refetchInterval(data) {
    const deletingComments = data?.filter(
      (comment) => !!comment.comment.metadata.deletionTimestamp
    );
    return deletingComments?.length ? 3000 : false;
  },
});

// Selection
const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedCommentNames.value =
      comments.value?.map((comment) => {
        return comment.comment.metadata.name;
      }) || [];
  } else {
    selectedCommentNames.value = [];
  }
};

const checkSelection = (comment: ListedComment) => {
  return (
    comment.comment.metadata.name ===
      selectedComment.value?.comment.metadata.name ||
    selectedCommentNames.value.includes(comment.comment.metadata.name)
  );
};

watch(
  () => selectedCommentNames.value,
  (newValue) => {
    checkAll.value = newValue.length === comments.value?.length;
  }
);

const handleDeleteInBatch = async () => {
  Dialog.warning({
    title: "确定要删除所选的评论吗？",
    description: "将同时删除所有评论下的回复，该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        const promises = selectedCommentNames.value.map((name) => {
          return apiClient.extension.comment.deletecontentHaloRunV1alpha1Comment(
            {
              name,
            }
          );
        });
        await Promise.all(promises);
        selectedCommentNames.value = [];

        Toast.success("删除成功");
      } catch (e) {
        console.error("Failed to delete comments", e);
      } finally {
        refetch();
      }
    },
  });
};

const handleApproveInBatch = async () => {
  Dialog.warning({
    title: "确定要审核通过所选的评论吗？",
    onConfirm: async () => {
      try {
        const commentsToUpdate = comments.value?.filter((comment) => {
          return (
            selectedCommentNames.value.includes(
              comment.comment.metadata.name
            ) && !comment.comment.spec.approved
          );
        });

        const promises = commentsToUpdate?.map((comment) => {
          return apiClient.extension.comment.updatecontentHaloRunV1alpha1Comment(
            {
              name: comment.comment.metadata.name,
              comment: {
                ...comment.comment,
                spec: {
                  ...comment.comment.spec,
                  approved: true,
                  // TODO: 暂时由前端设置发布时间。see https://github.com/halo-dev/halo/pull/2746
                  approvedTime: new Date().toISOString(),
                },
              },
            }
          );
        });
        await Promise.all(promises || []);
        selectedCommentNames.value = [];

        Toast.success("操作成功");
      } catch (e) {
        console.error("Failed to approve comments in batch", e);
      } finally {
        refetch();
      }
    },
  });
};
</script>
<template>
  <VPageHeader title="评论">
    <template #icon>
      <IconMessage class="mr-2 self-center" />
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
              v-permission="['system:comments:manage']"
              class="mr-4 hidden items-center sm:flex"
            >
              <input
                v-model="checkAll"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <div
                v-if="!selectedCommentNames.length"
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

                <FilterTag
                  v-if="selectedApprovedFilterItem.value != undefined"
                  @close="
                    handleApprovedFilterItemChange(ApprovedFilterItems[0])
                  "
                >
                  状态：{{ selectedApprovedFilterItem.label }}
                </FilterTag>

                <FilterTag
                  v-if="selectedUser"
                  @close="handleSelectUser(undefined)"
                >
                  评论者：{{ selectedUser?.spec.displayName }}
                </FilterTag>

                <FilterTag
                  v-if="selectedSortFilterItem.value != undefined"
                  @close="handleSortFilterItemChange(SortFilterItems[0])"
                >
                  排序：{{ selectedSortFilterItem.label }}
                </FilterTag>

                <FilterCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
              </div>
              <VSpace v-else>
                <VButton type="secondary" @click="handleApproveInBatch">
                  审核通过
                </VButton>
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
                    <span class="mr-0.5"> 状态 </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-72 p-4">
                      <ul class="space-y-1">
                        <li
                          v-for="(filterItem, index) in ApprovedFilterItems"
                          :key="index"
                          v-close-popper
                          :class="{
                            'bg-gray-100':
                              selectedApprovedFilterItem.value ===
                              filterItem.value,
                          }"
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleApprovedFilterItemChange(filterItem)"
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
                  v-model:selected="selectedUser"
                  @select="handleSelectUser"
                >
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">评论者</span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                </UserDropdownSelector>
                <FloatingDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5"> 排序 </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <div class="w-72 p-4">
                      <ul class="space-y-1">
                        <li
                          v-for="(filterItem, index) in SortFilterItems"
                          :key="index"
                          v-close-popper
                          :class="{
                            'bg-gray-100':
                              selectedSortFilterItem.value === filterItem.value,
                          }"
                          class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                          @click="handleSortFilterItemChange(filterItem)"
                        >
                          <span class="truncate">
                            {{ filterItem.label }}
                          </span>
                        </li>
                      </ul>
                    </div>
                  </template>
                </FloatingDropdown>
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="refetch()"
                  >
                    <IconRefreshLine
                      v-tooltip="`刷新`"
                      :class="{ 'animate-spin text-gray-900': isFetching }"
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
      <Transition v-else-if="!comments?.length" appear name="fade">
        <VEmpty message="你可以尝试刷新或者修改筛选条件" title="当前没有评论">
          <template #actions>
            <VSpace>
              <VButton @click="refetch">刷新</VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="comment in comments" :key="comment.comment.metadata.name">
            <CommentListItem
              :comment="comment"
              :is-selected="checkSelection(comment)"
              @reload="refetch()"
            >
              <template #checkbox>
                <input
                  v-model="selectedCommentNames"
                  :value="comment?.comment?.metadata.name"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  name="comment-checkbox"
                  type="checkbox"
                />
              </template>
            </CommentListItem>
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            v-model:page="page"
            v-model:size="size"
            :total="total"
            :size-options="[20, 30, 50, 100]"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
