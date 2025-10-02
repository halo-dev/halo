<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import SubjectQueryCommentListModal from "@console/modules/contents/comments/components/SubjectQueryCommentListModal.vue";
import type { ListedPost } from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import PostTag from "../../tags/components/PostTag.vue";

const props = withDefaults(
  defineProps<{
    post: ListedPost;
  }>(),
  {}
);

const { t } = useI18n();

const externalUrl = computed(() => {
  const { status, metadata } = props.post.post;
  if (metadata.labels?.[postLabels.PUBLISHED] === "true") {
    return status?.permalink;
  }
  return `/preview/posts/${metadata.name}`;
});

const commentSubjectRefKey = `content.halo.run/Post/${props.post.post.metadata.name}`;
const commentListVisible = ref(false);

const commentText = computed(() => {
  const { totalComment, approvedComment } = props.post.stats || {};

  let text = t("core.post.list.fields.comments", {
    comments: totalComment || 0,
  });

  const pendingComments = (totalComment || 0) - (approvedComment || 0);

  if (pendingComments > 0) {
    text += t("core.post.list.fields.comments-with-pending", {
      count: pendingComments,
    });
  }
  return text;
});
</script>

<template>
  <VEntityField
    :title="post.post.spec.title"
    :route="{
      name: 'PostEditor',
      query: { name: post.post.metadata.name },
    }"
    max-width="30rem"
  >
    <template #extra>
      <VSpace>
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
          class="text-gray-600 opacity-0 transition-all hover:text-gray-900 group-hover:opacity-100"
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
          <span
            class="cursor-pointer text-xs text-gray-500 hover:text-gray-900 hover:underline"
            @click="commentListVisible = true"
          >
            {{ commentText }}
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

      <SubjectQueryCommentListModal
        v-if="commentListVisible"
        :subject-ref-key="commentSubjectRefKey"
        @close="commentListVisible = false"
      />
    </template>
  </VEntityField>
</template>
