<script lang="ts" setup>
import { singlePageLabels } from "@/constants/labels";
import { consoleApiClient } from "@halo-dev/api-client";
import { IconPages, VCard } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";

const { data: total } = useQuery({
  queryKey: ["widget-singlePage-count"],
  queryFn: async () => {
    const { data } = await consoleApiClient.content.singlePage.listSinglePages({
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
  <VCard class="h-full" :body-class="['h-full']">
    <div class="flex h-full">
      <div class="flex items-center gap-4">
        <span
          class="hidden rounded-full bg-gray-100 p-2.5 text-gray-600 sm:block"
        >
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
  </VCard>
</template>
