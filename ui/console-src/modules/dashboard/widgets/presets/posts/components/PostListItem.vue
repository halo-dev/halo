<script lang="ts" setup>
import { postLabels } from "@/constants/labels";
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import SubjectQueryCommentListModal from "@console/modules/contents/comments/components/SubjectQueryCommentListModal.vue";
import type { ListedPost } from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  VEntity,
  VEntityField,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

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

const commentSubjectRefKey = `content.halo.run/Post/${props.post.post.metadata.name}`;
const commentListVisible = ref(false);

const commentText = computed(() => {
  const { totalComment, approvedComment } = props.post.stats || {};

  let text = t("core.dashboard.widgets.presets.recent_published.comments", {
    comments: totalComment || 0,
  });

  const pendingComments = (totalComment || 0) - (approvedComment || 0);

  if (pendingComments > 0) {
    text += t(
      "core.dashboard.widgets.presets.recent_published.comments-with-pending",
      {
        count: pendingComments,
      }
    );
  }
  return text;
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
            <span
              class="cursor-pointer text-xs text-gray-500 hover:text-gray-900 hover:underline"
              @click="commentListVisible = true"
            >
              {{ commentText }}
            </span>
          </VSpace>
          <SubjectQueryCommentListModal
            v-if="commentListVisible"
            :subject-ref-key="commentSubjectRefKey"
            @close="commentListVisible = false"
          />
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
              class="text-gray-600 opacity-0 transition-all hover:text-gray-900 group-hover:opacity-100"
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
