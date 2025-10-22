<script lang="ts" setup>
import type { ListedSinglePage } from "@halo-dev/api-client";
import { VEntityField } from "@halo-dev/components";
import { utils } from "@halo-dev/console-shared";

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
    v-tooltip="utils.date.format(singlePage.page.spec.publishTime)"
    :description="utils.date.timeAgo(singlePage.page.spec.publishTime)"
  >
  </VEntityField>
</template>
