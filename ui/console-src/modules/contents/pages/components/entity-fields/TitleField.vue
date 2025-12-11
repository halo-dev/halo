<script lang="ts" setup>
import { singlePageLabels } from "@/constants/labels";
import type { ListedSinglePage } from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    singlePage: ListedSinglePage;
  }>(),
  {}
);

const { t } = useI18n();

const externalUrl = computed(() => {
  const { metadata, status } = props.singlePage.page;
  if (metadata.labels?.[singlePageLabels.PUBLISHED] === "true") {
    return status?.permalink;
  }
  return `/preview/singlepages/${metadata.name}`;
});

const commentSubjectRefKey = `content.halo.run/SinglePage/${props.singlePage.page.metadata.name}`;
const commentListVisible = ref(false);

const commentText = computed(() => {
  const { totalComment, approvedComment } = props.singlePage.stats || {};

  let text = t("core.page.list.fields.comments", {
    comments: totalComment || 0,
  });

  const pendingComments = (totalComment || 0) - (approvedComment || 0);

  if (pendingComments > 0) {
    text += t("core.page.list.fields.comments-with-pending", {
      count: pendingComments,
    });
  }
  return text;
});
</script>

<template>
  <VEntityField
    :title="singlePage.page.spec.title"
    :route="{
      name: 'SinglePageEditor',
      query: { name: singlePage.page.metadata.name },
    }"
    max-width="30rem"
  >
    <template #extra>
      <VSpace>
        <RouterLink
          v-if="singlePage.page.status?.inProgress"
          v-tooltip="$t('core.common.tooltips.unpublished_content_tip')"
          :to="{
            name: 'SinglePageEditor',
            query: { name: singlePage.page.metadata.name },
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
      <div class="flex w-full flex-col gap-1">
        <VSpace class="w-full">
          <span class="text-xs text-gray-500">
            {{
              $t("core.page.list.fields.visits", {
                visits: singlePage.stats.visit || 0,
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
      </div>

      <SubjectQueryCommentListModal
        v-if="commentListVisible"
        :subject-ref-key="commentSubjectRefKey"
        @close="commentListVisible = false"
      />
    </template>
  </VEntityField>
</template>
