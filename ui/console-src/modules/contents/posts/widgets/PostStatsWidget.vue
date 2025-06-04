<script lang="ts" setup>
import { useDashboardStats } from "@console/composables/use-dashboard-stats";
import WidgetCard from "@console/modules/dashboard/components/WidgetCard.vue";
import { IconBookRead } from "@halo-dev/components";

const { data: stats } = useDashboardStats();
</script>
<template>
  <WidgetCard>
    <div class="flex h-full px-4 py-3">
      <div class="flex items-center gap-4">
        <span class="rounded-full bg-gray-100 p-2.5 text-gray-600">
          <IconBookRead class="h-5 w-5" />
        </span>

        <div>
          <span class="text-sm text-gray-500">
            {{ $t("core.dashboard.widgets.presets.post_stats.title") }}
          </span>
          <p class="text-2xl font-medium text-gray-900">
            {{ stats?.posts || 0 }}
          </p>
        </div>
      </div>
    </div>
  </WidgetCard>
</template>
