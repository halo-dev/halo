<script lang="ts" setup>
import {
  Dialog,
  IconExternalLinkLine,
  IconEye,
  IconEyeOff,
  Toast,
  VAvatar,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VSpace,
  VStatusDot,
} from "@halo-dev/components";
import PostTag from "../tags/components/PostTag.vue";
import { formatDatetime } from "@/utils/date";
import type { ListedPost, Post } from "@halo-dev/api-client";
import { useI18n } from "vue-i18n";
import { usePermission } from "@/utils/permission";
import { postLabels } from "@/constants/labels";
import { apiClient } from "@/utils/api-client";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { inject } from "vue";
import type { Ref } from "vue";
import { ref } from "vue";
import { computed } from "vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    post: ListedPost;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const emit = defineEmits<{
  (event: "open-setting-modal", post: Post): void;
}>();

const selectedPostNames = inject<Ref<string[]>>("selectedPostNames", ref([]));

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

const isPublishing = computed(() => {
  const { spec, status, metadata } = props.post.post;
  return (
    (spec.publish && metadata.labels?.[postLabels.PUBLISHED] !== "true") ||
    (spec.releaseSnapshot === spec.headSnapshot && status?.inProgress)
  );
});

const { mutate: changeVisibleMutation } = useMutation({
  mutationFn: async (post: Post) => {
    const { data } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: post.metadata.name,
      });
    data.spec.visible = data.spec.visible === "PRIVATE" ? "PUBLIC" : "PRIVATE";
    await apiClient.extension.post.updatecontentHaloRunV1alpha1Post(
      {
        name: post.metadata.name,
        post: data,
      },
      {
        mute: true,
      }
    );
    await queryClient.invalidateQueries({ queryKey: ["posts"] });
  },
  retry: 3,
  onSuccess: () => {
    Toast.success(t("core.common.toast.operation_success"));
  },
  onError: () => {
    Toast.error(t("core.common.toast.operation_failed"));
  },
});

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.post.operations.delete.title"),
    description: t("core.post.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await apiClient.post.recyclePost({
        name: props.post.post.metadata.name,
      });
      await queryClient.invalidateQueries({ queryKey: ["posts"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template
      v-if="currentUserHasPermission(['system:posts:manage'])"
      #checkbox
    >
      <input
        v-model="selectedPostNames"
        :value="post.post.metadata.name"
        class="h-4 w-4 rounded border-gray-300 text-indigo-600"
        name="post-checkbox"
        type="checkbox"
      />
    </template>
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
    <template #end>
      <VEntityField>
        <template #description>
          <RouterLink
            v-for="(contributor, contributorIndex) in post.contributors"
            :key="contributorIndex"
            :to="{
              name: 'UserDetail',
              params: { name: contributor.name },
            }"
            class="flex items-center"
          >
            <VAvatar
              v-tooltip="contributor.displayName"
              size="xs"
              :src="contributor.avatar"
              :alt="contributor.displayName"
              circle
            ></VAvatar>
          </RouterLink>
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
            @click="changeVisibleMutation(post.post)"
          />
          <IconEyeOff
            v-if="post.post.spec.visible === 'PRIVATE'"
            v-tooltip="$t('core.post.filters.visible.items.private')"
            class="cursor-pointer text-sm transition-all hover:text-blue-600"
            @click="changeVisibleMutation(post.post)"
          />
        </template>
      </VEntityField>
      <VEntityField v-if="post?.post?.spec.deleted">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{ formatDatetime(post.post.spec.publishTime) }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template
      v-if="currentUserHasPermission(['system:posts:manage'])"
      #dropdownItems
    >
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
      <VDropdownItem @click="emit('open-setting-modal', post.post)">
        {{ $t("core.common.buttons.setting") }}
      </VDropdownItem>
      <VDropdownDivider />
      <VDropdownItem type="danger" @click="handleDelete">
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
