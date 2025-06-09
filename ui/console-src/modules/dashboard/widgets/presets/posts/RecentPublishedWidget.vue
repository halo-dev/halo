<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import { formatDatetime } from "@/utils/date";
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import type { ListedPost } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  VButton,
  VEmpty,
  VEntity,
  VEntityContainer,
  VEntityField,
  VLoading,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";

const { data, isLoading, isFetching, refetch } = useQuery<ListedPost[]>({
  queryKey: ["widget-recent-posts"],
  queryFn: async () => {
    const { data } = await consoleApiClient.content.post.listPosts({
      labelSelector: [
        `${postLabels.DELETED}=false`,
        `${postLabels.PUBLISHED}=true`,
      ],
      sort: ["spec.publishTime,desc"],
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
    <OverlayScrollbarsComponent
      v-else
      element="div"
      :options="{ scrollbars: { autoHide: 'scroll' } }"
      class="h-full w-full"
      defer
    >
      <VEntityContainer>
        <VEntity v-for="post in data" :key="post.post.metadata.name">
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
                  <span class="truncate text-xs tabular-nums text-gray-500">
                    {{
                      $t(
                        "core.dashboard.widgets.presets.recent_published.publishTime",
                        {
                          publishTime: formatDatetime(
                            post.post.spec.publishTime
                          ),
                        }
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
        </VEntity>
      </VEntityContainer>
    </OverlayScrollbarsComponent>
  </WidgetCard>
</template>
