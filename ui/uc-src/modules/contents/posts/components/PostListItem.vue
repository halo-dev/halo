<script lang="ts" setup>
import StatusDotField from "@/components/entity-fields/StatusDotField.vue";
import HasPermission from "@/components/permission/HasPermission.vue";
import PostContributorList from "@/components/user/PostContributorList.vue";
import { postLabels } from "@/constants/labels";
import { formatDatetime } from "@/utils/date";
import PostTag from "@console/modules/contents/posts/tags/components/PostTag.vue";
import type { ListedPost } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconExternalLinkLine,
  IconEye,
  IconEyeOff,
  IconTimerLine,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { usePostPublishMutate } from "../composables/use-post-publish-mutate";

const { t } = useI18n();
const queryClient = useQueryClient();

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

const publishStatus = computed(() => {
  const { labels } = props.post.post.metadata;
  return labels?.[postLabels.PUBLISHED] === "true"
    ? t("core.post.filters.status.items.published")
    : t("core.post.filters.status.items.draft");
});

const isPublished = computed(() => {
  const {
    [postLabels.PUBLISHED]: published,
    [postLabels.SCHEDULING_PUBLISH]: schedulingPublish,
  } = props.post.post.metadata.labels || {};
  return published !== "true" && schedulingPublish !== "true";
});

const isPublishing = computed(() => {
  const { spec, metadata } = props.post.post;
  return (
    spec.publish &&
    metadata.labels?.[postLabels.PUBLISHED] !== "true" &&
    metadata.labels?.[postLabels.SCHEDULING_PUBLISH] !== "true"
  );
});

const { mutateAsync: postPublishMutate } = usePostPublishMutate();

async function handlePublish() {
  try {
    await postPublishMutate({ name: props.post.post.metadata.name });

    Toast.success(t("core.common.toast.publish_success"));
    queryClient.invalidateQueries({ queryKey: ["my-posts"] });
  } catch (_) {
    Toast.error(t("core.common.toast.publish_failed_and_retry"));
  }
}

function handleUnpublish() {
  Dialog.warning({
    title: t("core.uc_post.operations.cancel_publish.title"),
    description: t("core.uc_post.operations.cancel_publish.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await ucApiClient.content.post.unpublishMyPost({
        name: props.post.post.metadata.name,
      });

      Toast.success(t("core.common.toast.cancel_publish_success"));
      queryClient.invalidateQueries({ queryKey: ["my-posts"] });
    },
  });
}

function handleDelete() {
  Dialog.warning({
    title: t("core.uc_post.operations.delete.title"),
    description: t("core.uc_post.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await ucApiClient.content.post.recycleMyPost({
        name: props.post.post.metadata.name,
      });

      Toast.success(t("core.common.toast.delete_success"));

      queryClient.invalidateQueries({ queryKey: ["my-posts"] });
    },
  });
}
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
        width="27rem"
      >
        <template #extra>
          <VSpace class="mt-1 sm:mt-0">
            <RouterLink
              v-if="post.post.status?.inProgress"
              v-tooltip="$t('core.common.tooltips.unpublished_content_tip')"
              class="flex items-center"
              :to="{
                name: 'PostEditor',
                query: { name: post.post.metadata.name },
              }"
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
                :route="false"
              ></PostTag>
            </VSpace>
          </div>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField>
        <template #description>
          <PostContributorList
            :owner="post.owner"
            :contributors="post.contributors"
            :allow-view-user-detail="false"
          />
        </template>
      </VEntityField>
      <VEntityField :description="publishStatus">
        <template v-if="isPublishing" #description>
          <VStatusDot :text="$t('core.common.tooltips.publishing')" animate />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <IconEye
            v-if="post.post.spec.visible === 'PUBLIC'"
            v-tooltip="$t('core.post.filters.visible.items.public')"
            class="cursor-pointer text-sm transition-all hover:text-blue-600"
          />
          <IconEyeOff
            v-if="post.post.spec.visible === 'PRIVATE'"
            v-tooltip="$t('core.post.filters.visible.items.private')"
            class="cursor-pointer text-sm transition-all hover:text-blue-600"
          />
        </template>
      </VEntityField>
      <StatusDotField
        v-if="props.post.post.spec.deleted"
        :tooltip="$t('core.common.status.deleting')"
        state="warning"
        animate
      />
      <VEntityField v-if="post.post.spec.publishTime">
        <template #description>
          <div class="inline-flex items-center space-x-2">
            <span class="entity-field-description">
              {{ formatDatetime(post.post.spec.publishTime) }}
            </span>
            <IconTimerLine
              v-if="
                post.post.metadata.labels?.[postLabels.SCHEDULING_PUBLISH] ===
                'true'
              "
              v-tooltip="$t('core.post.list.fields.schedule_publish.tooltip')"
              class="text-sm"
            />
          </div>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <HasPermission v-if="isPublished" :permissions="['uc:posts:publish']">
        <VDropdownItem @click="handlePublish">
          {{ $t("core.common.buttons.publish") }}
        </VDropdownItem>
      </HasPermission>
      <VDropdownItem
        @click="
          $router.push({
            name: 'PostEditor',
            query: { name: post.post.metadata.name },
          })
        "
      >
        {{ $t("core.common.buttons.edit") }}
      </VDropdownItem>
      <HasPermission v-if="!isPublished" :permissions="['uc:posts:publish']">
        <VDropdownItem type="danger" @click="handleUnpublish">
          {{ $t("core.common.buttons.cancel_publish") }}
        </VDropdownItem>
      </HasPermission>
      <HasPermission :permissions="['uc:posts:recycle']">
        <VDropdownDivider />
        <VDropdownItem type="danger" @click="handleDelete">
          {{ $t("core.common.buttons.delete") }}
        </VDropdownItem>
      </HasPermission>
    </template>
  </VEntity>
</template>
