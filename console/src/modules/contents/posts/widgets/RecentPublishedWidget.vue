<script lang="ts" setup>
import {
  VCard,
  VSpace,
  VEntity,
  VEntityField,
  IconExternalLinkLine,
} from "@halo-dev/components";
import type { ListedPost } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { postLabels } from "@/constants/labels";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";

const { data } = useQuery<ListedPost[]>({
  queryKey: ["widget-recent-posts"],
  queryFn: async () => {
    const { data } = await apiClient.post.listPosts({
      labelSelector: [
        `${postLabels.DELETED}=false`,
        `${postLabels.PUBLISHED}=true`,
      ],
      sort: "PUBLISH_TIME",
      sortOrder: false,
      page: 1,
      size: 10,
    });
    return data.items;
  },
});
</script>
<template>
  <VCard
    :body-class="['h-full', '!p-0', '!overflow-auto']"
    class="h-full"
    :title="$t('core.dashboard.widgets.presets.recent_published.title')"
  >
    <OverlayScrollbarsComponent
      element="div"
      :options="{ scrollbars: { autoHide: 'scroll' } }"
      class="h-full w-full"
      defer
    >
      <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
        <li v-for="(post, index) in data" :key="index">
          <VEntity>
            <template #start>
              <VEntityField
                :title="post.post.spec.title"
                :route="{
                  name: 'PostEditor',
                  query: { name: post.post.metadata.name },
                }"
              >
                <template #description>
                  <VSpace>
                    <span class="text-xs text-gray-500">
                      {{
                        $t(
                          "core.dashboard.widgets.presets.recent_published.visits",
                          { visits: post.stats.visit || 0 }
                        )
                      }}
                    </span>
                    <span class="text-xs text-gray-500">
                      {{
                        $t(
                          "core.dashboard.widgets.presets.recent_published.comments",
                          { comments: post.stats.totalComment || 0 }
                        )
                      }}
                    </span>
                  </VSpace>
                </template>
                <template #extra>
                  <a
                    v-if="post.post.status?.permalink"
                    target="_blank"
                    :href="post.post.status?.permalink"
                    :title="post.post.status?.permalink"
                    class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
                  >
                    <IconExternalLinkLine class="h-3.5 w-3.5" />
                  </a>
                </template>
              </VEntityField>
            </template>
            <template #end>
              <VEntityField>
                <template #description>
                  <span class="truncate text-xs tabular-nums text-gray-500">
                    {{ formatDatetime(post.post.spec.publishTime) }}
                  </span>
                </template>
              </VEntityField>
            </template>
          </VEntity>
        </li>
      </ul>
    </OverlayScrollbarsComponent>
  </VCard>
</template>
