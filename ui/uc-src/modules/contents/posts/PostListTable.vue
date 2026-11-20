<script lang="ts" setup>
import PostTag from "@console/modules/contents/posts/tags/components/PostTag.vue";
import type { ListedPost } from "@halo-dev/api-client";
import { GetThumbnailByUriSizeEnum, ucApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconBookRead,
  IconExternalLinkLine,
  IconEye,
  IconEyeOff,
  IconMore,
  IconRefreshLine,
  IconTimerLine,
  Toast,
  VButton,
  VCard,
  VDropdown,
  VDropdownDivider,
  VDropdownItem,
  VEmpty,
  VFilterDropdown,
  VFilterDropdownGroup,
  VPageHeader,
  VPagination,
  VSpace,
  VStatusDot,
  VTable,
  createTableColumnHelper,
  type ColumnPinningState,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import PostContributorList from "@/components/user/PostContributorList.vue";
import { postLabels } from "@/constants/labels";
import { usePostPublishMutate } from "./composables/use-post-publish-mutate";

const { t } = useI18n();
const queryClient = useQueryClient();

const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
const keyword = useRouteQuery<string>("keyword", "");

const columnPinning = ref<ColumnPinningState>({
  start: ["post"],
  end: ["actions"],
});

const selectedPublishPhase = useRouteQuery<
  "DRAFT" | "PENDING_APPROVAL" | "PUBLISHED" | "FAILED" | undefined
>("status");

watch(
  () => [selectedPublishPhase.value, keyword.value],
  () => {
    page.value = 1;
  }
);

const {
  data: posts,
  isLoading,
  isFetching,
  refetch,
} = useQuery({
  queryKey: ["my-posts-table-demo", page, size, keyword, selectedPublishPhase],
  queryFn: async () => {
    const labelSelector: string[] = ["content.halo.run/deleted=false"];
    const { data } = await ucApiClient.content.post.listMyPosts({
      labelSelector,
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      publishPhase: selectedPublishPhase.value,
    });
    return data;
  },
  onSuccess(data) {
    page.value = data.page;
    size.value = data.size;
  },
  refetchInterval: (data) => {
    const hasDeletingPosts = data?.items.some((post) => post.post.spec.deleted);

    if (hasDeletingPosts) {
      return 1000;
    }

    const hasPublishingPost = data?.items.some((post) => {
      const { spec, metadata } = post.post;
      return (
        metadata.labels?.[postLabels.PUBLISHED] !== spec.publish + "" &&
        metadata.labels?.[postLabels.SCHEDULING_PUBLISH] !== "true"
      );
    });

    if (hasPublishingPost) {
      return 1000;
    }

    const hasCancelingPublishPost = data?.items.some((post) => {
      const { spec, metadata } = post.post;
      return (
        !spec.publish &&
        (metadata.labels?.[postLabels.PUBLISHED] === "true" ||
          metadata.labels?.[postLabels.SCHEDULING_PUBLISH] === "true")
      );
    });

    return hasCancelingPublishPost ? 1000 : false;
  },
});

const { mutateAsync: postPublishMutate } = usePostPublishMutate();

function externalUrl(post: ListedPost) {
  const { status, metadata } = post.post;
  if (metadata.labels?.[postLabels.PUBLISHED] === "true") {
    return status?.permalink;
  }
  return `/preview/posts/${metadata.name}`;
}

function publishStatus(post: ListedPost) {
  const { labels } = post.post.metadata;
  return labels?.[postLabels.PUBLISHED] === "true"
    ? t("core.post.filters.status.items.published")
    : t("core.post.filters.status.items.draft");
}

function isPublished(post: ListedPost) {
  const {
    [postLabels.PUBLISHED]: published,
    [postLabels.SCHEDULING_PUBLISH]: schedulingPublish,
  } = post.post.metadata.labels || {};
  return published !== "true" && schedulingPublish !== "true";
}

function isPublishing(post: ListedPost) {
  const { spec, metadata } = post.post;
  return (
    spec.publish &&
    metadata.labels?.[postLabels.PUBLISHED] !== "true" &&
    metadata.labels?.[postLabels.SCHEDULING_PUBLISH] !== "true"
  );
}

async function handlePublish(post: ListedPost) {
  try {
    await postPublishMutate({ name: post.post.metadata.name });
    Toast.success(t("core.common.toast.publish_success"));
    queryClient.invalidateQueries({ queryKey: ["my-posts-table-demo"] });
  } catch (_) {
    Toast.error(t("core.common.toast.publish_failed_and_retry"));
  }
}

function handleUnpublish(post: ListedPost) {
  Dialog.warning({
    title: t("core.uc_post.operations.cancel_publish.title"),
    description: t("core.uc_post.operations.cancel_publish.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await ucApiClient.content.post.unpublishMyPost({
        name: post.post.metadata.name,
      });
      Toast.success(t("core.common.toast.cancel_publish_success"));
      queryClient.invalidateQueries({ queryKey: ["my-posts-table-demo"] });
    },
  });
}

function handleDelete(post: ListedPost) {
  Dialog.warning({
    title: t("core.uc_post.operations.delete.title"),
    description: t("core.uc_post.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await ucApiClient.content.post.recycleMyPost({
        name: post.post.metadata.name,
      });
      Toast.success(t("core.common.toast.delete_success"));
      queryClient.invalidateQueries({ queryKey: ["my-posts-table-demo"] });
    },
  });
}

function getPostRowKey(post: ListedPost) {
  return post.post.metadata.name;
}

const column = createTableColumnHelper<ListedPost>();

const columns = column.columns([
  column.display({
    id: "post",
    header: "文章",
    meta: { minWidth: 360 },
  }),
  column.display({
    id: "status",
    header: "状态",
    meta: { width: 110 },
  }),
  column.display({
    id: "visibility",
    header: "可见性",
    meta: { width: 100, align: "center" },
  }),
  column.display({
    id: "contributors",
    header: "作者",
    meta: { width: 170 },
  }),
  column.display({
    id: "stats",
    header: "数据",
    meta: { width: 150 },
  }),
  column.accessor((post: ListedPost) => post.post.spec.publishTime, {
    id: "publishTime",
    header: "发布时间",
    enableSorting: true,
    sortFn: "datetime",
    meta: { width: 150 },
  }),
  column.display({
    id: "actions",
    header: "操作",
    meta: { width: 90, align: "right" },
  }),
]);
</script>

<template>
  <VPageHeader :title="$t('core.uc_post.title')">
    <template #icon>
      <IconBookRead />
    </template>
    <template #actions>
      <VButton :route="{ name: 'PostEditor' }" type="secondary">
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
            class="relative flex flex-col flex-wrap items-start justify-between gap-4 sm:flex-row sm:items-center"
          >
            <SearchInput v-model="keyword" />

            <VSpace spacing="lg" class="flex-wrap">
              <VFilterDropdownGroup
                :clear-label="$t('core.common.buttons.reset')"
              >
                <VFilterDropdown
                  v-model="selectedPublishPhase"
                  :label="$t('core.common.filters.labels.status')"
                  show-clear-option
                  :clear-label="$t('core.common.filters.item_labels.all')"
                  :options="[
                    {
                      label: $t('core.post.filters.status.items.published'),
                      value: 'PUBLISHED',
                    },
                    {
                      label: $t('core.post.filters.status.items.draft'),
                      value: 'DRAFT',
                    },
                  ]"
                />
              </VFilterDropdownGroup>
              <button
                v-tooltip="$t('core.common.buttons.refresh')"
                type="button"
                class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                @click="refetch()"
              >
                <IconRefreshLine
                  :class="{ 'animate-spin text-gray-900': isFetching }"
                  class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                />
              </button>
            </VSpace>
          </div>
        </div>
      </template>

      <VTable
        v-model:column-pinning="columnPinning"
        :columns="columns"
        :data="posts?.items || []"
        :loading="isLoading"
        :row-key="getPostRowKey"
        header-menu
        pinnable
        resizable
      >
        <template #empty>
          <VEmpty
            :message="$t('core.post.empty.message')"
            :title="$t('core.post.empty.title')"
          >
            <template #actions>
              <VSpace>
                <VButton @click="refetch">
                  {{ $t("core.common.buttons.refresh") }}
                </VButton>
              </VSpace>
            </template>
          </VEmpty>
        </template>

        <template #cell-post="{ original }">
          <div class="flex items-start gap-3">
            <div
              v-if="original.post.spec.cover"
              class="aspect-h-2 aspect-w-3 w-16 shrink-0 overflow-hidden rounded-md"
            >
              <img
                class="h-full w-full object-cover"
                :src="
                  utils.attachment.getThumbnailUrl(
                    original.post.spec.cover,
                    GetThumbnailByUriSizeEnum.S
                  )
                "
                @error="
                  utils.attachment.handleThumbnailError(
                    $event,
                    original.post.spec.cover
                  )
                "
              />
            </div>
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <RouterLink
                  :to="{
                    name: 'PostEditor',
                    query: { name: original.post.metadata.name },
                  }"
                  class="truncate font-medium text-gray-900 hover:text-primary"
                >
                  {{ original.post.spec.title }}
                </RouterLink>
                <RouterLink
                  v-if="original.post.status?.inProgress"
                  v-tooltip="$t('core.common.tooltips.unpublished_content_tip')"
                  :to="{
                    name: 'PostEditor',
                    query: { name: original.post.metadata.name },
                  }"
                  class="flex shrink-0 items-center"
                >
                  <VStatusDot state="success" animate />
                </RouterLink>
                <a
                  target="_blank"
                  :href="externalUrl(original)"
                  class="shrink-0 text-gray-400 hover:text-gray-900"
                >
                  <IconExternalLinkLine class="h-3.5 w-3.5" />
                </a>
              </div>
              <div
                class="mt-1 flex flex-wrap gap-x-3 gap-y-1 text-xs text-gray-500"
              >
                <span v-if="original.categories.length">
                  {{ $t("core.post.list.fields.categories") }}
                  <a
                    v-for="(category, categoryIndex) in original.categories"
                    :key="categoryIndex"
                    :href="category.status?.permalink"
                    :title="category.status?.permalink"
                    target="_blank"
                    class="hover:text-gray-900"
                  >
                    {{ category.spec.displayName }}
                  </a>
                </span>
                <span v-if="original.post.spec.pinned">
                  {{ $t("core.post.list.fields.pinned") }}
                </span>
              </div>
              <div
                v-if="original.tags.length"
                class="mt-1 flex flex-wrap gap-1"
              >
                <PostTag
                  v-for="(tag, tagIndex) in original.tags"
                  :key="tagIndex"
                  :tag="tag"
                  :route="false"
                />
              </div>
            </div>
          </div>
        </template>

        <template #cell-status="{ original }">
          <VStatusDot
            v-if="original.post.spec.deleted"
            :text="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
          <VStatusDot
            v-else-if="isPublishing(original)"
            :text="$t('core.common.tooltips.publishing')"
            animate
          />
          <span v-else class="text-sm text-gray-600">
            {{ publishStatus(original) }}
          </span>
        </template>

        <template #cell-visibility="{ original }">
          <IconEye
            v-if="original.post.spec.visible === 'PUBLIC'"
            v-tooltip="$t('core.post.filters.visible.items.public')"
            class="text-sm text-gray-500 hover:text-blue-600"
          />
          <IconEyeOff
            v-if="original.post.spec.visible === 'PRIVATE'"
            v-tooltip="$t('core.post.filters.visible.items.private')"
            class="text-sm text-gray-500 hover:text-blue-600"
          />
        </template>

        <template #cell-contributors="{ original }">
          <PostContributorList
            :owner="original.owner"
            :contributors="original.contributors"
            :allow-view-user-detail="false"
          />
        </template>

        <template #cell-stats="{ original }">
          <div class="flex flex-col gap-1 text-xs text-gray-500">
            <span>
              {{
                $t("core.post.list.fields.visits", {
                  visits: original.stats.visit,
                })
              }}
            </span>
            <span>
              {{
                $t("core.post.list.fields.comments", {
                  comments: original.stats.totalComment || 0,
                })
              }}
            </span>
          </div>
        </template>

        <template #cell-publishTime="{ original }">
          <div
            v-if="original.post.spec.publishTime"
            class="inline-flex items-center gap-1 text-sm text-gray-500"
          >
            <span v-tooltip="utils.date.format(original.post.spec.publishTime)">
              {{ utils.date.timeAgo(original.post.spec.publishTime) }}
            </span>
            <IconTimerLine
              v-if="
                original.post.metadata.labels?.[
                  postLabels.SCHEDULING_PUBLISH
                ] === 'true'
              "
              v-tooltip="$t('core.post.list.fields.schedule_publish.tooltip')"
              class="text-sm"
            />
          </div>
          <span v-else class="text-sm text-gray-400">-</span>
        </template>

        <template #cell-actions="{ original }">
          <VDropdown>
            <button
              aria-label="更多操作"
              class="inline-flex cursor-pointer rounded p-1 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900"
              type="button"
            >
              <IconMore />
            </button>
            <template #popper>
              <HasPermission
                v-if="isPublished(original)"
                :permissions="['uc:posts:publish']"
              >
                <VDropdownItem @click="handlePublish(original)">
                  {{ $t("core.common.buttons.publish") }}
                </VDropdownItem>
              </HasPermission>
              <VDropdownItem
                @click="
                  $router.push({
                    name: 'PostEditor',
                    query: { name: original.post.metadata.name },
                  })
                "
              >
                {{ $t("core.common.buttons.edit") }}
              </VDropdownItem>
              <HasPermission
                v-if="!isPublished(original)"
                :permissions="['uc:posts:publish']"
              >
                <VDropdownItem type="danger" @click="handleUnpublish(original)">
                  {{ $t("core.common.buttons.cancel_publish") }}
                </VDropdownItem>
              </HasPermission>
              <HasPermission :permissions="['uc:posts:recycle']">
                <VDropdownDivider />
                <VDropdownItem type="danger" @click="handleDelete(original)">
                  {{ $t("core.common.buttons.delete") }}
                </VDropdownItem>
              </HasPermission>
            </template>
          </VDropdown>
        </template>
      </VTable>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', {
              total: posts?.total || 0,
            })
          "
          :total="posts?.total || 0"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
