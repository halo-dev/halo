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
  VDropdown,
  VDropdownItem,
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
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const checkAll = ref(false);
const selectedComment = ref<ListedComment>();
const selectedCommentNames = ref<string[]>([]);
const keyword = ref("");

// Filters
const ApprovedFilterItems: { label: string; value?: boolean }[] = [
  {
    label: t("core.comment.filters.status.items.all"),
    value: undefined,
  },
  {
    label: t("core.comment.filters.status.items.approved"),
    value: true,
  },
  {
    label: t("core.comment.filters.status.items.pending_review"),
    value: false,
  },
];

type Sort = "LAST_REPLY_TIME" | "REPLY_COUNT" | "CREATE_TIME";

const SortFilterItems: {
  label: string;
  value?: Sort;
}[] = [
  {
    label: t("core.comment.filters.sort.items.default"),
    value: undefined,
  },
  {
    label: t("core.comment.filters.sort.items.last_reply_time"),
    value: "LAST_REPLY_TIME",
  },
  {
    label: t("core.comment.filters.sort.items.reply_count"),
    value: "REPLY_COUNT",
  },
  {
    label: t("core.comment.filters.sort.items.creation_time"),
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
    title: t("core.comment.operations.delete_comment_in_batch.title"),
    description: t(
      "core.comment.operations.delete_comment_in_batch.description"
    ),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
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

        Toast.success(t("core.common.toast.delete_success"));
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
    title: t("core.comment.operations.approve_comment_in_batch.title"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
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

        Toast.success(t("core.common.toast.operation_success"));
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
  <VPageHeader :title="$t('core.comment.title')">
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
                  :placeholder="$t('core.common.placeholder.search')"
                  type="text"
                  name="keyword"
                  :model-value="keyword"
                  @keyup.enter="handleKeywordChange"
                ></FormKit>

                <FilterTag v-if="keyword" @close="handleClearKeyword()">
                  {{
                    $t("core.common.filters.results.keyword", {
                      keyword: keyword,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedApprovedFilterItem.value != undefined"
                  @close="
                    handleApprovedFilterItemChange(ApprovedFilterItems[0])
                  "
                >
                  {{
                    $t("core.common.filters.results.status", {
                      status: selectedApprovedFilterItem.label,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedUser"
                  @close="handleSelectUser(undefined)"
                >
                  {{
                    $t("core.comment.filters.owner.result", {
                      owner: selectedUser.spec.displayName,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedSortFilterItem.value != undefined"
                  @close="handleSortFilterItemChange(SortFilterItems[0])"
                >
                  {{
                    $t("core.common.filters.results.sort", {
                      sort: selectedSortFilterItem.label,
                    })
                  }}
                </FilterTag>

                <FilterCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
              </div>
              <VSpace v-else>
                <VButton type="secondary" @click="handleApproveInBatch">
                  {{
                    $t(
                      "core.comment.operations.approve_comment_in_batch.button"
                    )
                  }}
                </VButton>
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
                      {{ $t("core.common.filters.labels.status") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(filterItem, index) in ApprovedFilterItems"
                      :key="index"
                      :selected="
                        selectedApprovedFilterItem.value === filterItem.value
                      "
                      @click="handleApprovedFilterItemChange(filterItem)"
                    >
                      {{ filterItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <UserDropdownSelector
                  v-model:selected="selectedUser"
                  @select="handleSelectUser"
                >
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.comment.filters.owner.label") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                </UserDropdownSelector>
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
                      v-for="(filterItem, index) in SortFilterItems"
                      :key="index"
                      :selected="
                        selectedSortFilterItem.value === filterItem.value
                      "
                      @click="handleSortFilterItemChange(filterItem)"
                    >
                      {{ filterItem.label }}
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
        <VEmpty
          :message="$t('core.comment.empty.message')"
          :title="$t('core.comment.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                {{ $t("core.common.buttons.refresh") }}
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
            :page-label="$t('core.components.pagination.page_label')"
            :size-label="$t('core.components.pagination.size_label')"
            :total="total"
            :size-options="[20, 30, 50, 100]"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
