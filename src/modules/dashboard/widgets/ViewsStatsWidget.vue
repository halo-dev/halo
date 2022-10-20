<script lang="ts" name="ViewsStatsWidget" setup>
import { apiClient } from "@/utils/api-client";
import type { DashboardStats } from "@halo-dev/api-client";
import { VCard } from "@halo-dev/components";
import { onMounted, ref } from "vue";

const stats = ref<DashboardStats>({
  visits: 0,
});

const handleFetchStats = async () => {
  const { data } = await apiClient.stats.getStats();
  stats.value = data;
};

onMounted(handleFetchStats);
</script>
<template>
  <VCard class="h-full">
    <dt class="truncate text-sm font-medium text-gray-500">浏览量</dt>
    <dd class="mt-1 text-3xl font-semibold text-gray-900">
      {{ stats.visits || 0 }}
    </dd>
  </VCard>
</template>
