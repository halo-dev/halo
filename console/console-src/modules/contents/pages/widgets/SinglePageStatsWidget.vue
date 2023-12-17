<script lang="ts" setup>
import { VCard, IconPages } from "@halo-dev/components";
import { onMounted, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import { singlePageLabels } from "@/constants/labels";

const singlePageTotal = ref<number>(0);

const handleFetchSinglePages = async () => {
  const { data } = await apiClient.singlePage.listSinglePages({
    labelSelector: [
      `${singlePageLabels.DELETED}=false`,
      `${singlePageLabels.PUBLISHED}=true`,
    ],
    page: 0,
    size: 0,
  });
  singlePageTotal.value = data.total;
};

onMounted(handleFetchSinglePages);
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
            {{ singlePageTotal || 0 }}
          </p>
        </div>
      </div>
    </div>
  </VCard>
</template>
