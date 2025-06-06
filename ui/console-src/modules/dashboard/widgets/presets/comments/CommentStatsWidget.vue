<script lang="ts" setup>
import { useDashboardStats } from "@console/composables/use-dashboard-stats";
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import { IconMessage } from "@halo-dev/components";
import NumberFlow from "@number-flow/vue";

defineProps<{
  config: {
    enable_animation: boolean;
  };
}>();

const { data: stats } = useDashboardStats();
</script>
<template>
  <WidgetCard>
    <div class="flex h-full px-4 py-3">
      <div class="flex items-center gap-4">
        <span class="rounded-full bg-gray-100 p-2.5 text-gray-600">
          <IconMessage class="h-5 w-5" />
        </span>

        <div class="flex flex-col">
          <span class="text-sm text-gray-500">
            {{ $t("core.dashboard.widgets.presets.comment_stats.title") }}
          </span>
          <NumberFlow
            v-if="config.enable_animation"
            class="text-2xl font-medium text-gray-900"
            :value="stats?.approvedComments || 0"
            :format="{ notation: 'compact' }"
          >
          </NumberFlow>
          <span v-else class="text-2xl font-medium text-gray-900">
            {{ stats?.approvedComments || 0 }}
          </span>
        </div>
      </div>
    </div>
  </WidgetCard>
</template>
