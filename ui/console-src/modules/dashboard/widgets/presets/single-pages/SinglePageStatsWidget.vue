<script lang="ts" setup>
import { singlePageLabels } from "@/constants/labels";
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import { coreApiClient } from "@halo-dev/api-client";
import { IconPages } from "@halo-dev/components";
import NumberFlow from "@number-flow/vue";
import { useQuery } from "@tanstack/vue-query";

defineProps<{
  config: {
    enable_animation: boolean;
  };
}>();

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

        <div class="flex flex-col">
          <span class="text-sm text-gray-500">
            {{ $t("core.dashboard.widgets.presets.page_stats.title") }}
          </span>
          <NumberFlow
            v-if="config.enable_animation"
            class="text-2xl font-medium text-gray-900"
            :value="total || 0"
            :format="{ notation: 'compact' }"
          >
          </NumberFlow>
          <span v-else class="text-2xl font-medium text-gray-900">
            {{ total || 0 }}
          </span>
        </div>
      </div>
    </div>
  </WidgetCard>
</template>
