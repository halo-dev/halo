<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import type { ListedPost } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { VEntityContainer } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import PostListItem from "./components/PostListItem.vue";

const { data } = useQuery<ListedPost[]>({
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
    <OverlayScrollbarsComponent
      element="div"
      :options="{ scrollbars: { autoHide: 'scroll' } }"
      class="h-full w-full"
      defer
    >
      <VEntityContainer>
        <PostListItem
          v-for="post in data"
          :key="post.post.metadata.name"
          :post="post"
        />
      </VEntityContainer>
    </OverlayScrollbarsComponent>
  </WidgetCard>
</template>
