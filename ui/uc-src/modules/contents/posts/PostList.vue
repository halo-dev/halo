<script lang="ts" setup>
import { ucApiClient } from "@halo-dev/api-client";
import {
  IconAddCircle,
  IconBookRead,
  IconRefreshLine,
  VButton,
  VCard,
  VEmpty,
  VEntityContainer,
  VFilterDropdown,
  VFilterDropdownGroup,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { watch } from "vue";
import { postLabels } from "@/constants/labels";
import PostListItem from "./components/PostListItem.vue";

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
    <VCard flush>
      <template #toolbar>
        <div
          class="relative flex flex-col flex-wrap items-start justify-between gap-4 sm:flex-row sm:items-center"
        >
          <SearchInput v-model="keyword" />

          <VSpace spacing="sm" wrap>
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
            <VButton
              :label="$t('core.common.buttons.refresh')"
              variant="ghost"
              size="sm"
              @click="refetch()"
            >
              <template #icon>
                <IconRefreshLine :class="{ 'animate-spin': isFetching }" />
              </template>
            </VButton>
          </VSpace>
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
                type="secondary"
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
          <PostListItem
            v-for="post in posts.items"
            :key="post.post.metadata.name"
            :post="post"
          />
        </VEntityContainer>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :total="posts?.total || 0"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
