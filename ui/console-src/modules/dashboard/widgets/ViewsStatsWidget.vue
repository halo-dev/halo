<script lang="ts" setup>
import { useDashboardStats } from "@console/composables/use-dashboard-stats";
import { IconEye } from "@halo-dev/components";
import WidgetCard from "../components/WidgetCard.vue";

const { data: stats } = useDashboardStats();
</script>
<template>
  <WidgetCard>
    <div class="flex h-full px-4 py-3">
      <div class="flex items-center gap-4">
        <span class="rounded-full bg-gray-100 p-2.5 text-gray-600">
          <IconEye class="h-5 w-5" />
        </span>

        <div>
          <span class="text-sm text-gray-500">
            {{ $t("core.dashboard.widgets.presets.views_stats.title") }}
          </span>
          <p class="text-2xl font-medium text-gray-900">
            {{ stats?.visits || 0 }}
          </p>
        </div>
      </div>
    </div>
  </WidgetCard>
</template>
