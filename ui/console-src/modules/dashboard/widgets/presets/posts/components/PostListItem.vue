<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import type { ListedPost } from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  VEntity,
  VEntityField,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { computed } from "vue";

const props = defineProps<{
  post: ListedPost;
}>();

const isPublished = computed(() => {
  return props.post.post.metadata.labels?.[postLabels.PUBLISHED] === "true";
});

const previewUrl = computed(() => {
  const { status, metadata } = props.post.post;
  if (isPublished.value) {
    return status?.permalink;
  }
  return `/preview/posts/${metadata.name}`;
});

const datetime = computed(() => {
  return (
    props.post.post.spec.publishTime ||
    props.post.post.metadata.creationTimestamp
  );
});
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField
        :title="post.post.spec.title"
        :route="{
          name: 'PostEditor',
          query: { name: post.post.metadata.name },
        }"
      >
        <template v-if="isPublished" #description>
          <VSpace>
            <span class="text-xs text-gray-500">
              {{
                $t("core.dashboard.widgets.presets.recent_published.visits", {
                  visits: post.stats.visit || 0,
                })
              }}
            </span>
            <span class="text-xs text-gray-500">
              {{
                $t("core.dashboard.widgets.presets.recent_published.comments", {
                  comments: post.stats.totalComment || 0,
                })
              }}
            </span>
          </VSpace>
        </template>
        <template #extra>
          <VSpace>
            <VTag v-if="!isPublished">
              {{ $t("core.post.filters.status.items.draft") }}
            </VTag>
            <a
              v-if="previewUrl"
              target="_blank"
              :href="previewUrl"
              :title="previewUrl"
              class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
            >
              <IconExternalLinkLine class="h-3.5 w-3.5" />
            </a>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField
        v-tooltip="formatDatetime(datetime)"
        :description="relativeTimeTo(datetime)"
      ></VEntityField>
    </template>
  </VEntity>
</template>
