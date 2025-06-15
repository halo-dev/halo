<script lang="ts" setup>
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import type { ListedSinglePage } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";

withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
  }>(),
  {}
);
</script>

<template>
  <VEntityField
    v-if="singlePage.page.spec.publishTime"
    v-tooltip="formatDatetime(singlePage.page.spec.publishTime)"
    :description="relativeTimeTo(singlePage.page.spec.publishTime)"
  >
  </VEntityField>
</template>
