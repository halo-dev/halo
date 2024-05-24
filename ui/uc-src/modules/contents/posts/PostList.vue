<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";
import {
  IconAddCircle,
  IconBookRead,
  IconRefreshLine,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import PostListItem from "./components/PostListItem.vue";
import { useRouteQuery } from "@vueuse/router";
import { computed, watch } from "vue";
import { postLabels } from "@/constants/labels";

const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
const keyword = useRouteQuery<string>("keyword", "");

const selectedPublishPhase = useRouteQuery<
  "DRAFT" | "PENDING_APPROVAL" | "PUBLISHED" | "FAILED" | undefined
>("status");

function handleClearFilters() {
  selectedPublishPhase.value = undefined;
}

const hasFilters = computed(() => {
  return selectedPublishPhase.value !== undefined;
});

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
  queryKey: ["my-posts", page, size, keyword, selectedPublishPhase],
  queryFn: async () => {
    const labelSelector: string[] = ["content.halo.run/deleted=false"];
    const { data } = await apiClient.uc.post.listMyPosts({
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
</script>

<template>
  <VPageHeader :title="$t('core.uc_post.title')">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton :route="{ name: 'PostEditor' }" type="secondary">
        <template #icon>
          <IconAddCircle class="h-full w-full" />
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
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
              />
              <FilterDropdown
                v-model="selectedPublishPhase"
                :label="$t('core.common.filters.labels.status')"
                :items="[
                  {
                    label: $t('core.common.filters.item_labels.all'),
                    value: undefined,
                  },
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
            </VSpace>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!posts?.items.length" appear name="fade">
        <VEmpty
          :message="$t('core.post.empty.message')"
          :title="$t('core.post.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton
                v-permission="['system:posts:manage']"
                :route="{ name: 'PostEditor' }"
                type="primary"
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
          <li v-for="post in posts.items" :key="post.post.metadata.name">
            <PostListItem :post="post" />
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
