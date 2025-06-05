<script lang="ts" setup>
import { singlePageLabels } from "@/constants/labels";
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import { coreApiClient } from "@halo-dev/api-client";
import { IconPages } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";

const { data: total } = useQuery({
  queryKey: ["widget-singlePage-count"],
  queryFn: async () => {
    const { data } = await coreApiClient.content.singlePage.listSinglePage({
      labelSelector: [
        `${singlePageLabels.DELETED}=false`,
        `${singlePageLabels.PUBLISHED}=true`,
      ],
      page: 0,
      size: 0,
    });
    return data.total;
  },
});
</script>
<template>
  <WidgetCard>
    <div class="flex h-full px-4 py-3">
      <div class="flex items-center gap-4">
        <span class="rounded-full bg-gray-100 p-2.5 text-gray-600">
          <IconPages class="h-5 w-5" />
        </span>

        <div>
          <span class="text-sm text-gray-500">
            {{ $t("core.dashboard.widgets.presets.page_stats.title") }}
          </span>
          <p class="text-2xl font-medium text-gray-900">
            {{ total || 0 }}
          </p>
        </div>
      </div>
    </div>
  </WidgetCard>
</template>
