<script lang="ts" setup>
import type { DashboardStats } from "@halo-dev/api-client";
import { VCard, IconMessage } from "@halo-dev/components";
import { inject, type Ref } from "vue";

const dashboardStats = inject<Ref<DashboardStats>>("dashboardStats");
</script>
<template>
  <VCard class="h-full" :body-class="['h-full']">
    <div class="flex h-full">
      <div class="flex items-center gap-4">
        <span
          class="hidden rounded-full bg-gray-100 p-2.5 text-gray-600 sm:block"
        >
          <IconMessage class="h-5 w-5" />
        </span>

        <div>
          <span class="text-sm text-gray-500">
            {{ $t("core.dashboard.widgets.presets.comment_stats.title") }}
          </span>
          <p class="text-2xl font-medium text-gray-900">
            {{ dashboardStats?.approvedComments }}
          </p>
        </div>
      </div>
    </div>
  </VCard>
</template>
