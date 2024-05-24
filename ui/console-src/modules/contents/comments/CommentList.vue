<script lang="ts" setup>
import {
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
import type { ListedComment } from "@halo-dev/api-client";
import { computed, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import UserFilterDropdown from "@/components/filter/UserFilterDropdown.vue";
import { useRouteQuery } from "@vueuse/router";

const { t } = useI18n();

const checkAll = ref(false);
const selectedComment = ref<ListedComment>();
const selectedCommentNames = ref<string[]>([]);

const keyword = useRouteQuery<string>("keyword", "");
const selectedApprovedStatus = useRouteQuery<
  string | undefined,
  boolean | undefined
>("approved", undefined, {
  transform: (value) => {
    return value ? value === "true" : undefined;
  },
});
const selectedSort = useRouteQuery<string | undefined>("sort");
const selectedUser = useRouteQuery<string | undefined>("user");

watch(
  () => [
    selectedApprovedStatus.value,
    selectedSort.value,
    selectedUser.value,
    keyword.value,
  ],
  () => {
    page.value = 1;
  }
);

const hasFilters = computed(() => {
  return (
    selectedApprovedStatus.value !== undefined ||
    selectedSort.value ||
    selectedUser.value
  );
});

function handleClearFilters() {
  selectedApprovedStatus.value = undefined;
  selectedSort.value = undefined;
  selectedUser.value = undefined;
}

const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
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
    selectedApprovedStatus,
    selectedSort,
    selectedUser,
    keyword,
  ],
  queryFn: async () => {
    const fieldSelectorMap: Record<string, string | boolean | undefined> = {
      "spec.approved": selectedApprovedStatus.value,
    };

    const fieldSelector = Object.entries(fieldSelectorMap)
      .map(([key, value]) => {
        if (value !== undefined) {
          return `${key}=${value}`;
        }
      })
      .filter(Boolean) as string[];

    const { data } = await apiClient.comment.listComments({
      fieldSelector,
      page: page.value,
      size: size.value,
      sort: [selectedSort.value].filter(Boolean) as string[],
      keyword: keyword.value,
      ownerName: selectedUser.value,
      // TODO: email users are not supported at the moment.
      ownerKind: selectedUser.value ? "User" : undefined,
    });

    total.value = data.total;

    return data.items;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.some(
      (comment) => !!comment.comment.metadata.deletionTimestamp
    );
    return hasDeletingData ? 1000 : false;
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
          return apiClient.extension.comment.deleteContentHaloRunV1alpha1Comment(
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
          return apiClient.extension.comment.updateContentHaloRunV1alpha1Comment(
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
            class="relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <div
              v-permission="['system:comments:manage']"
              class="hidden items-center sm:flex"
            >
              <input
                v-model="checkAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <SearchInput
                v-if="!selectedCommentNames.length"
                v-model="keyword"
              />
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
            <VSpace spacing="lg" class="flex-wrap">
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
              />
              <FilterDropdown
                v-model="selectedApprovedStatus"
                :label="$t('core.common.filters.labels.status')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.all'),
                  },
                  {
                    label: t('core.comment.filters.status.items.approved'),
                    value: true,
                  },
                  {
                    label: t(
                      'core.comment.filters.status.items.pending_review'
                    ),
                    value: false,
                  },
                ]"
              />
              <HasPermission :permissions="['system:users:view']">
                <UserFilterDropdown
                  v-model="selectedUser"
                  :label="$t('core.comment.filters.owner.label')"
                />
              </HasPermission>
              <FilterDropdown
                v-model="selectedSort"
                :label="$t('core.common.filters.labels.sort')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.default'),
                  },
                  {
                    label: t(
                      'core.comment.filters.sort.items.last_reply_time_desc'
                    ),
                    value: 'status.lastReplyTime,desc',
                  },
                  {
                    label: t(
                      'core.comment.filters.sort.items.last_reply_time_asc'
                    ),
                    value: 'status.lastReplyTime,asc',
                  },
                  {
                    label: t(
                      'core.comment.filters.sort.items.reply_count_desc'
                    ),
                    value: 'status.replyCount,desc',
                  },
                  {
                    label: t('core.comment.filters.sort.items.reply_count_asc'),
                    value: 'status.replyCount,asc',
                  },
                  {
                    label: t(
                      'core.comment.filters.sort.items.create_time_desc'
                    ),
                    value: 'metadata.creationTimestamp,desc',
                  },
                  {
                    label: t('core.comment.filters.sort.items.create_time_asc'),
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
                    :class="{ 'animate-spin text-gray-900': isFetching }"
                    class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                  />
                </div>
              </div>
            </VSpace>
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
            >
              <template #checkbox>
                <input
                  v-model="selectedCommentNames"
                  :value="comment?.comment?.metadata.name"
                  name="comment-checkbox"
                  type="checkbox"
                />
              </template>
            </CommentListItem>
          </li>
        </ul>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', { total: total })
          "
          :total="total"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
