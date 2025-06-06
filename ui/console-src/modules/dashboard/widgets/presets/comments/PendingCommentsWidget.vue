<script lang="ts" setup>
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import type { ListedComment } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { VEntityContainer } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import CommentItem from "./CommentItem.vue";

const { data } = useQuery<ListedComment[]>({
  queryKey: ["widget-pending-comments"],
  queryFn: async () => {
    const { data } = await consoleApiClient.content.comment.listComments({
      fieldSelector: ["spec.approved=false"],
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
    :title="
      $t(
        'core.dashboard.widgets.presets.pending_comments.title',
        'Pending Comments'
      )
    "
  >
    <OverlayScrollbarsComponent
      element="div"
      :options="{ scrollbars: { autoHide: 'scroll' } }"
      class="h-full w-full"
      defer
    >
      <VEntityContainer>
        <CommentItem
          v-for="comment in data"
          :key="comment.comment.metadata.name"
          :comment="comment"
        ></CommentItem>
      </VEntityContainer>
    </OverlayScrollbarsComponent>
  </WidgetCard>
</template>
