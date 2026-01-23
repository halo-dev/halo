<script setup lang="ts">
import CommentDetailModal from "@console/modules/contents/comments/components/CommentDetailModal.vue";
import OwnerButton from "@console/modules/contents/comments/components/OwnerButton.vue";
import { useContentProviderExtensionPoint } from "@console/modules/contents/comments/composables/use-content-provider-extension-point";
import { useSubjectRef } from "@console/modules/contents/comments/composables/use-subject-ref";
import { coreApiClient, type ListedComment } from "@halo-dev/api-client";
import {
  Dialog,
  IconExternalLinkLine,
  Toast,
  VEntity,
  VEntityField,
  VStatusDot,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const props = defineProps<{
  comment: ListedComment;
}>();

const { t } = useI18n();
const queryClient = useQueryClient();
const { subjectRefResult } = useSubjectRef(props.comment);

const detailModalVisible = ref(false);

function onCommentDetailModalClose() {
  detailModalVisible.value = false;
  queryClient.invalidateQueries({ queryKey: ["widget-pending-comments"] });
}

const creationTime = computed(() => {
  return (
    props.comment?.comment.spec.creationTime ||
    props.comment?.comment.metadata.creationTimestamp
  );
});

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.comment.operations.delete_comment.title"),
    description: t("core.comment.operations.delete_comment.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await coreApiClient.content.comment.deleteComment({
          name: props.comment?.comment?.metadata.name as string,
        });

        Toast.success(t("core.common.toast.delete_success"));
      } catch (error) {
        console.error("Failed to delete comment", error);
      } finally {
        queryClient.invalidateQueries({
          queryKey: ["widget-pending-comments"],
        });
      }
    },
  });
};

const { data: contentProvider } = useContentProviderExtensionPoint();
</script>

<template>
  <CommentDetailModal
    v-if="detailModalVisible"
    :comment="comment"
    @close="onCommentDetailModalClose"
  />
  <VEntity>
    <template #start>
      <VEntityField width="100%" max-width="100%">
        <template #description>
          <div class="flex flex-col gap-2">
            <div class="mb-1 flex items-center gap-2">
              <OwnerButton
                :owner="comment?.owner"
                @click="detailModalVisible = true"
              />
              <span class="whitespace-nowrap text-sm text-gray-900">
                {{ $t("core.comment.text.commented_on") }}
              </span>
              <RouterLink
                v-tooltip="`${subjectRefResult.label}`"
                :to="subjectRefResult.route || $route"
                class="inline-block max-w-md truncate text-sm font-medium text-gray-900 hover:text-gray-600"
              >
                {{ subjectRefResult.title }}
              </RouterLink>
              <a
                v-if="subjectRefResult.externalUrl"
                :href="subjectRefResult.externalUrl"
                target="_blank"
                class="invisible text-gray-600 hover:text-gray-900 group-hover:visible"
              >
                <IconExternalLinkLine class="h-3.5 w-3.5" />
              </a>
            </div>
            <component
              :is="contentProvider?.component"
              :content="comment?.comment?.spec.content"
            />
            <HasPermission :permissions="['system:comments:manage']">
              <div class="flex items-center gap-3 text-xs">
                <span
                  class="cursor-pointer select-none text-gray-700 hover:text-gray-900"
                  @click="detailModalVisible = true"
                >
                  {{ $t("core.comment.operations.review.button") }}
                </span>
                <span
                  class="cursor-pointer select-none text-gray-700 hover:text-red-600"
                  @click="handleDelete"
                >
                  {{ $t("core.common.buttons.delete") }}
                </span>
              </div>
            </HasPermission>
          </div>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="!comment?.comment.spec.approved">
        <template #description>
          <VStatusDot
            state="warning"
            animate
            :text="$t('core.comment.list.fields.pending_review')"
          />
        </template>
      </VEntityField>
      <VEntityField v-if="comment?.comment?.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot
            v-tooltip="$t('core.common.status.deleting')"
            state="warning"
            animate
          />
        </template>
      </VEntityField>
      <VEntityField
        v-tooltip="utils.date.format(creationTime)"
        :description="utils.date.timeAgo(creationTime)"
      />
    </template>
  </VEntity>
</template>
