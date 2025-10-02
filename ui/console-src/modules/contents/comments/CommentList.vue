<script lang="ts" setup>
import UserFilterDropdown from "@/components/filter/UserFilterDropdown.vue";
import type { ListedComment } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconMessage,
  IconRefreshLine,
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
import { useRouteQuery } from "@vueuse/router";
import { chunk } from "lodash-es";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import CommentListItem from "./components/CommentListItem.vue";
import useCommentsFetch from "./composables/use-comments-fetch";

const { t } = useI18n();

const checkAll = ref(false);
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

const {
  data: comments,
  isLoading,
  isFetching,
  refetch,
} = useCommentsFetch(
  "core:comments",
  page,
  size,
  selectedApprovedStatus,
  selectedSort,
  selectedUser,
  keyword
);

// Selection
const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedCommentNames.value =
      comments.value?.items.map((comment) => {
        return comment.comment.metadata.name;
      }) || [];
  } else {
    selectedCommentNames.value = [];
  }
};

const isSelection = (comment: ListedComment) => {
  return selectedCommentNames.value.includes(comment.comment.metadata.name);
};

watch(
  () => selectedCommentNames.value,
  (newValue) => {
    checkAll.value = newValue.length === comments.value?.items.length;
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
        const commentChunk = chunk(selectedCommentNames.value, 5);

        for (const item of commentChunk) {
          await Promise.all(
            item.map((name) => {
              return coreApiClient.content.comment.deleteComment({
                name,
              });
            })
          );
        }

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
        const commentsToUpdate = comments.value?.items.filter((comment) => {
          return (
            selectedCommentNames.value.includes(
              comment.comment.metadata.name
            ) && !comment.comment.spec.approved
          );
        });

        if (!commentsToUpdate?.length) {
          return;
        }

        const commentChunk = chunk(commentsToUpdate, 5);

        for (const item of commentChunk) {
          await Promise.all(
            item.map((comment) => {
              return coreApiClient.content.comment.patchComment({
                name: comment.comment.metadata.name,
                jsonPatchInner: [
                  {
                    op: "add",
                    path: "/spec/approved",
                    value: true,
                  },
                  {
                    op: "add",
                    path: "/spec/approvedTime",
                    value: new Date().toISOString(),
                  },
                ],
              });
            })
          );
        }

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
      <IconMessage />
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
      <Transition v-else-if="!comments?.items.length" appear name="fade">
        <VEmpty
          :message="$t('core.comment.empty.message')"
          :title="$t('core.comment.empty.title')"
        >
          <template #actions>
            <VButton @click="refetch">
              {{ $t("core.common.buttons.refresh") }}
            </VButton>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <VEntityContainer>
          <CommentListItem
            v-for="comment in comments.items"
            :key="comment.comment.metadata.name"
            :comment="comment"
            :is-selected="isSelection(comment)"
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
        </VEntityContainer>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', {
              total: comments?.total || 0,
            })
          "
          :total="comments?.total || 0"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
