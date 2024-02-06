<script lang="ts" setup>
import {
  IconExternalLinkLine,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import PostTag from "../../tags/components/PostTag.vue";
import type { ListedPost } from "@halo-dev/api-client";
import { postLabels } from "@/constants/labels";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    post: ListedPost;
  }>(),
  {}
);

const externalUrl = computed(() => {
  const { status, metadata } = props.post.post;
  if (metadata.labels?.[postLabels.PUBLISHED] === "true") {
    return status?.permalink;
  }
  return `/preview/posts/${metadata.name}`;
});
</script>

<template>
  <VEntityField
    :title="post.post.spec.title"
    :route="{
      name: 'PostEditor',
      query: { name: post.post.metadata.name },
    }"
    width="27rem"
  >
    <template #extra>
      <VSpace class="mt-1 sm:mt-0">
        <RouterLink
          v-if="post.post.status?.inProgress"
          v-tooltip="$t('core.common.tooltips.unpublished_content_tip')"
          :to="{
            name: 'PostEditor',
            query: { name: post.post.metadata.name },
          }"
          class="flex items-center"
        >
          <VStatusDot state="success" animate />
        </RouterLink>
        <a
          target="_blank"
          :href="externalUrl"
          class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
        >
          <IconExternalLinkLine class="h-3.5 w-3.5" />
        </a>
      </VSpace>
    </template>
    <template #description>
      <div class="flex flex-col gap-1.5">
        <VSpace class="flex-wrap !gap-y-1">
          <p
            v-if="post.categories.length"
            class="inline-flex flex-wrap gap-1 text-xs text-gray-500"
          >
            {{ $t("core.post.list.fields.categories") }}
            <a
              v-for="(category, categoryIndex) in post.categories"
              :key="categoryIndex"
              :href="category.status?.permalink"
              :title="category.status?.permalink"
              target="_blank"
              class="cursor-pointer hover:text-gray-900"
            >
              {{ category.spec.displayName }}
            </a>
          </p>
          <span class="text-xs text-gray-500">
            {{
              $t("core.post.list.fields.visits", {
                visits: post.stats.visit,
              })
            }}
          </span>
          <span class="text-xs text-gray-500">
            {{
              $t("core.post.list.fields.comments", {
                comments: post.stats.totalComment || 0,
              })
            }}
          </span>
          <span v-if="post.post.spec.pinned" class="text-xs text-gray-500">
            {{ $t("core.post.list.fields.pinned") }}
          </span>
        </VSpace>
        <VSpace v-if="post.tags.length" class="flex-wrap">
          <PostTag
            v-for="(tag, tagIndex) in post.tags"
            :key="tagIndex"
            :tag="tag"
            route
          ></PostTag>
        </VSpace>
      </div>
    </template>
  </VEntityField>
</template>
