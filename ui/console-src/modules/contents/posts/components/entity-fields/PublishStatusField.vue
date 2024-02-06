<script lang="ts" setup>
import { VEntityField, VStatusDot } from "@halo-dev/components";
import type { ListedPost } from "@halo-dev/api-client";
import { postLabels } from "@/constants/labels";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    post: ListedPost;
  }>(),
  {}
);

const publishStatus = computed(() => {
  const { labels } = props.post.post.metadata;
  return labels?.[postLabels.PUBLISHED] === "true"
    ? t("core.post.filters.status.items.published")
    : t("core.post.filters.status.items.draft");
});

const isPublishing = computed(() => {
  const { spec, status, metadata } = props.post.post;
  return (
    (spec.publish && metadata.labels?.[postLabels.PUBLISHED] !== "true") ||
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
