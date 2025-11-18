<script lang="ts" setup>
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconExternalLinkLine,
  Toast,
  VButton,
  VDropdownDivider,
  VDropdownItem,
  VEmpty,
  VEntity,
  VEntityContainer,
  VEntityField,
  VLoading,
  VSpace,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { utils, type OperationItem } from "@halo-dev/ui-shared";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, markRaw, provide, ref, toRefs, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import { useCommentLastReadTimeMutate } from "../composables/use-comment-last-readtime-mutate";
import { useContentProviderExtensionPoint } from "../composables/use-content-provider-extension-point";
import { useSubjectRef } from "../composables/use-subject-ref";
import CommentDetailModal from "./CommentDetailModal.vue";
import OwnerButton from "./OwnerButton.vue";
import ReplyCreationModal from "./ReplyCreationModal.vue";
import ReplyListItem from "./ReplyListItem.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    comment: ListedComment;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const { comment } = toRefs(props);

const hoveredReply = ref<ListedReply>();
const showReplies = ref(false);
const replyModal = ref(false);
const detailModalVisible = ref(false);

provide<Ref<ListedReply | undefined>>("hoveredReply", hoveredReply);

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
        queryClient.invalidateQueries({ queryKey: ["core:comments"] });
      }
    },
  });
};

const handleApproveReplyInBatch = async () => {
  Dialog.warning({
    title: t("core.comment.operations.approve_applies_in_batch.title"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        const repliesToUpdate = replies.value?.filter((reply) => {
          return !reply.reply.spec.approved;
        });

        if (!repliesToUpdate?.length) {
          return;
        }

        await Promise.all(
          repliesToUpdate?.map((reply) => {
            return coreApiClient.content.reply.patchReply({
              name: reply.reply.metadata.name,
              jsonPatchInner: [
                {
                  op: "add",
                  path: "/spec/approved",
                  value: true,
                },
                {
                  op: "add",
                  path: "/spec/approvedTime",
                  value: new Date().toISOString(),
                },
              ],
            });
          })
        );

        Toast.success(t("core.common.toast.operation_success"));
      } catch (e) {
        console.error("Failed to approve comment replies in batch", e);
      } finally {
        await refetch();
      }
    },
  });
};

async function handleCancelApprove() {
  await coreApiClient.content.comment.patchComment({
    name: props.comment.comment.metadata.name,
    jsonPatchInner: [
      {
        op: "add",
        path: "/spec/approved",
        value: false,
      },
      {
        op: "add",
        path: "/spec/approvedTime",
        value: "",
      },
    ],
  });

  Toast.success(t("core.common.toast.operation_success"));

  queryClient.invalidateQueries({ queryKey: ["core:comments"] });
}

const {
  data: replies,
  isLoading,
  refetch,
} = useQuery<ListedReply[]>({
  queryKey: [
    "core:comment-replies",
    props.comment.comment.metadata.name,
    showReplies,
  ],
  queryFn: async () => {
    const { data } = await consoleApiClient.content.reply.listReplies({
      commentName: props.comment.comment.metadata.name,
      page: 0,
      size: 0,
    });
    return data.items;
  },
  refetchInterval(data) {
    const hasDeletingReplies = data?.some(
      (reply) => !!reply.reply.metadata.deletionTimestamp
    );
    return hasDeletingReplies ? 1000 : false;
  },
  enabled: computed(() => showReplies.value),
});

const { mutate: updateCommentLastReadTimeMutate } =
  useCommentLastReadTimeMutate(props.comment);

const handleToggleShowReplies = async () => {
  showReplies.value = !showReplies.value;

  if (props.comment.comment.status?.unreadReplyCount && showReplies.value) {
    updateCommentLastReadTimeMutate();
  }
};

const onReplyCreationModalClose = () => {
  if (showReplies.value) {
    refetch();
  }

  queryClient.invalidateQueries({ queryKey: ["core:comments"] });
  updateCommentLastReadTimeMutate();
  replyModal.value = false;
  detailModalVisible.value = false;
};

const { subjectRefResult } = useSubjectRef(props.comment);

const { data: operationItems } = useOperationItemExtensionPoint<ListedComment>(
  "comment:list-item:operation:create",
  comment,
  computed((): OperationItem<ListedComment>[] => [
    {
      priority: 0,
      component: markRaw(VDropdownItem),
      label: t("core.comment.operations.review.button"),
      action: () => {
        detailModalVisible.value = true;
      },
      hidden: props.comment?.comment.spec.approved,
    },
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.text.detail"),
      hidden: !props.comment?.comment.spec.approved,
      action: () => {
        detailModalVisible.value = true;
      },
    },
    {
      priority: 20,
      component: markRaw(VDropdownItem),
      label: t("core.comment.operations.approve_applies_in_batch.button"),
      action: handleApproveReplyInBatch,
    },
    {
      priority: 30,
      component: markRaw(VDropdownDivider),
    },
    {
      priority: 40,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.comment.operations.cancel_approve.button"),
      hidden: !props.comment?.comment.spec.approved,
      action: handleCancelApprove,
    },
    {
      priority: 50,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.delete"),
      action: handleDelete,
    },
  ])
);

const { data: contentProvider } = useContentProviderExtensionPoint();
</script>

<template>
  <ReplyCreationModal
    v-if="replyModal"
    :comment="comment"
    @close="onReplyCreationModalClose"
  />
  <CommentDetailModal
    v-if="detailModalVisible"
    :comment="comment"
    @close="onReplyCreationModalClose"
  />
  <VEntity :is-selected="isSelected">
    <template
      v-if="utils.permission.has(['system:comments:manage']) && $slots.checkbox"
      #checkbox
    >
      <slot name="checkbox" />
    </template>
    <template #start>
      <VEntityField width="100%" max-width="100%">
        <template #description>
          <div class="flex flex-col gap-2">
            <div class="mb-1 flex items-center gap-2">
              <OwnerButton
                :owner="comment?.owner"
                @click="detailModalVisible = true"
              />
              <VTag v-if="comment.comment.spec.hidden">
                {{ $t("core.comment.list.fields.private") }}
              </VTag>
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
            <div class="flex items-center gap-3 text-xs">
              <span
                class="cursor-pointer select-none text-gray-700 hover:text-gray-900"
                @click="handleToggleShowReplies"
              >
                {{
                  $t("core.comment.list.fields.reply_count", {
                    count: comment?.comment?.status?.replyCount || 0,
                  })
                }}
              </span>
              <VStatusDot
                v-show="(comment?.comment?.status?.unreadReplyCount || 0) > 0"
                v-tooltip="$t('core.comment.list.fields.has_new_replies')"
                state="success"
                animate
              />
              <HasPermission :permissions="['system:comments:manage']">
                <span
                  class="cursor-pointer select-none text-gray-700 hover:text-gray-900"
                  @click="replyModal = true"
                >
                  {{ $t("core.comment.operations.reply.button") }}
                </span>
              </HasPermission>
            </div>
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
    <template
      v-if="utils.permission.has(['system:comments:manage'])"
      #dropdownItems
    >
      <EntityDropdownItems
        :dropdown-items="operationItems || []"
        :item="comment"
      />
    </template>

    <template v-if="showReplies" #footer>
      <div class="pl-8">
        <VLoading v-if="isLoading" />
        <Transition v-else-if="!replies?.length" appear name="fade">
          <VEmpty
            :message="$t('core.comment.reply_empty.message')"
            :title="$t('core.comment.reply_empty.title')"
          >
            <template #actions>
              <VSpace>
                <VButton @click="refetch()">
                  {{ $t("core.common.buttons.refresh") }}
                </VButton>
                <HasPermission :permissions="['system:comments:manage']">
                  <VButton type="secondary" @click="replyModal = true">
                    <template #icon>
                      <IconAddCircle />
                    </template>
                    {{ $t("core.comment.reply_empty.new") }}
                  </VButton>
                </HasPermission>
              </VSpace>
            </template>
          </VEmpty>
        </Transition>
        <Transition v-else appear name="fade">
          <VEntityContainer>
            <ReplyListItem
              v-for="reply in replies"
              :key="reply.reply.metadata.name"
              :reply="reply"
              :comment="comment"
              :replies="replies"
            ></ReplyListItem>
          </VEntityContainer>
        </Transition>
      </div>
    </template>
  </VEntity>
</template>
