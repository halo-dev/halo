<script lang="ts" setup>
import { singlePageLabels } from "@/constants/labels";
import type { ListedSinglePage } from "@halo-dev/api-client";
import { VEntityField, VStatusDot } from "@halo-dev/components";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
  }>(),
  {}
);

const publishStatus = computed(() => {
  const { labels } = props.singlePage.page.metadata;
  return labels?.[singlePageLabels.PUBLISHED] === "true"
    ? t("core.page.filters.status.items.published")
    : t("core.page.filters.status.items.draft");
});

const isPublishing = computed(() => {
  const { spec, status, metadata } = props.singlePage.page;
  return (
    (spec.publish &&
      metadata.labels?.[singlePageLabels.PUBLISHED] !== "true") ||
    (spec.releaseSnapshot === spec.headSnapshot && status?.inProgress)
  );
});
</script>

<template>
  <VEntityField :description="publishStatus">
    <template v-if="isPublishing" #description>
      <VStatusDot :text="$t('core.common.tooltips.publishing')" animate />
    </template>
  </VEntityField>
</template>
