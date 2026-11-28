<script lang="ts" setup>
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import type { ListedPost } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  VButton,
  VEmpty,
  VEntityContainer,
  VLoading,
  VScrollArea,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { postLabels } from "@/constants/labels";
import PostListItem from "./components/PostListItem.vue";

const { data, isLoading, isFetching, refetch } = useQuery<ListedPost[]>({
  queryKey: ["widget-recent-posts"],
  queryFn: async () => {
    const { data } = await consoleApiClient.content.post.listPosts({
      labelSelector: [`${postLabels.DELETED}=false`],
      page: 1,
      size: 10,
    });
    return data.items;
  },
});
</script>
<template>
  <WidgetCard
    :body-class="['!overflow-auto']"
    :title="$t('core.dashboard.widgets.presets.recent_published.title')"
  >
    <VLoading v-if="isLoading" />
    <VEmpty
      v-else-if="!data?.length"
      :title="$t('core.dashboard.widgets.presets.recent_published.empty.title')"
    >
      <template #actions>
        <VButton :loading="isFetching" @click="refetch">
          {{ $t("core.common.buttons.refresh") }}
        </VButton>
      </template>
    </VEmpty>
    <VScrollArea v-else class="h-full w-full">
      <VEntityContainer>
        <PostListItem
          v-for="post in data"
          :key="post.post.metadata.name"
          :post="post"
        />
      </VEntityContainer>
    </VScrollArea>
  </WidgetCard>
</template>
