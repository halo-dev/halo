<script lang="ts" setup>
import {
  Dialog,
  VAvatar,
  VButton,
  VEntity,
  VEntityField,
  VStatusDot,
  VSpace,
  VEmpty,
  IconAddCircle,
  IconExternalLinkLine,
  VLoading,
  Toast,
  VDropdownItem,
  VTag,
} from "@halo-dev/components";
import ReplyCreationModal from "./ReplyCreationModal.vue";
import type {
  Extension,
  ListedComment,
  ListedReply,
  Post,
  SinglePage,
} from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { computed, provide, ref, type Ref } from "vue";
import ReplyListItem from "./ReplyListItem.vue";
import { apiClient } from "@/utils/api-client";
import type { RouteLocationRaw } from "vue-router";
import cloneDeep from "lodash.clonedeep";
import { usePermission } from "@/utils/permission";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    comment: ListedComment;
    isSelected?: boolean;
  }>(),
  {
    comment: undefined,
    isSelected: false,
  }
);

const emit = defineEmits<{
  (event: "reload"): void;
}>();

const selectedReply = ref<ListedReply>();
const hoveredReply = ref<ListedReply>();
const showReplies = ref(false);
const replyModal = ref(false);

provide<Ref<ListedReply | undefined>>("hoveredReply", hoveredReply);

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.comment.operations.delete_comment.title"),
    description: t("core.comment.operations.delete_comment.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await apiClient.extension.comment.deletecontentHaloRunV1alpha1Comment({
          name: props.comment?.comment?.metadata.name as string,
        });

        Toast.success(t("core.common.toast.delete_success"));
      } catch (error) {
        console.error("Failed to delete comment", error);
      } finally {
        emit("reload");
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
        const promises = repliesToUpdate?.map((reply) => {
          return apiClient.extension.reply.updatecontentHaloRunV1alpha1Reply({
            name: reply.reply.metadata.name,
            reply: {
              ...reply.reply,
              spec: {
                ...reply.reply.spec,
                approved: true,
                // TODO: 暂时由前端设置发布时间。see https://github.com/halo-dev/halo/pull/2746
                approvedTime: new Date().toISOString(),
              },
            },
          });
        });
        await Promise.all(promises || []);

        Toast.success(t("core.common.toast.operation_success"));
      } catch (e) {
        console.error("Failed to approve comment replies in batch", e);
      } finally {
        await refetch();
      }
    },
  });
};

const handleApprove = async () => {
  try {
    const commentToUpdate = cloneDeep(props.comment.comment);
    commentToUpdate.spec.approved = true;
    // TODO: 暂时由前端设置发布时间。see https://github.com/halo-dev/halo/pull/2746
    commentToUpdate.spec.approvedTime = new Date().toISOString();
    await apiClient.extension.comment.updatecontentHaloRunV1alpha1Comment({
      name: commentToUpdate.metadata.name,
      comment: commentToUpdate,
    });

    Toast.success(t("core.common.toast.operation_success"));
  } catch (error) {
    console.error("Failed to approve comment", error);
  } finally {
    emit("reload");
  }
};

const {
  data: replies,
  isLoading,
  refetch,
} = useQuery<ListedReply[]>({
  queryKey: [
    "comment-replies",
    props.comment.comment.metadata.name,
    showReplies,
  ],
  queryFn: async () => {
    const { data } = await apiClient.reply.listReplies({
      commentName: props.comment.comment.metadata.name,
      page: 0,
      size: 0,
    });
    return data.items;
  },
  refetchInterval(data) {
    const deletingReplies = data?.filter(
      (reply) => !!reply.reply.metadata.deletionTimestamp
    );
    return deletingReplies?.length ? 3000 : false;
  },
  enabled: computed(() => showReplies.value),
});

const handleToggleShowReplies = async () => {
  showReplies.value = !showReplies.value;
  if (showReplies.value) {
    // update last read time
    if (props.comment.comment.status?.unreadReplyCount) {
      const commentToUpdate = cloneDeep(props.comment.comment);
      commentToUpdate.spec.lastReadTime = new Date().toISOString();
      await apiClient.extension.comment.updatecontentHaloRunV1alpha1Comment({
        name: commentToUpdate.metadata.name,
        comment: commentToUpdate,
      });
    }
  } else {
    emit("reload");
  }
};

const handleTriggerReply = () => {
  replyModal.value = true;
};

const onTriggerReply = (reply: ListedReply) => {
  selectedReply.value = reply;
  replyModal.value = true;
};

const onReplyCreationModalClose = () => {
  selectedReply.value = undefined;
  refetch();
};

// Subject ref processing
interface SubjectRefResult {
  label: string;
  title: string;
  route?: RouteLocationRaw;
  externalUrl?: string;
}

const SubjectRefProvider = ref<
  Record<string, (subject: Extension) => SubjectRefResult>[]
>([
  {
    Post: (subject: Extension): SubjectRefResult => {
      const post = subject as Post;
      return {
        label: t("core.comment.subject_refs.post"),
        title: post.spec.title,
        externalUrl: post.status?.permalink,
        route: {
          name: "PostEditor",
          query: {
            name: post.metadata.name,
          },
        },
      };
    },
  },
  {
    SinglePage: (subject: Extension): SubjectRefResult => {
      const singlePage = subject as SinglePage;
      return {
        label: t("core.comment.subject_refs.page"),
        title: singlePage.spec.title,
        externalUrl: singlePage.status?.permalink,
        route: {
          name: "SinglePageEditor",
          query: {
            name: singlePage.metadata.name,
          },
        },
      };
    },
  },
]);

const subjectRefResult = computed(() => {
  const { subject } = props.comment;
  if (!subject) {
    return {
      label: t("core.comment.subject_refs.unknown"),
      title: t("core.comment.subject_refs.unknown"),
    };
  }
  const subjectRef = SubjectRefProvider.value.find((provider) =>
    Object.keys(provider).includes(subject.kind)
  );
  if (!subjectRef) {
    return {
      label: t("core.comment.subject_refs.unknown"),
      title: t("core.comment.subject_refs.unknown"),
    };
  }
  return subjectRef[subject.kind](subject);
});
</script>

<template>
  <ReplyCreationModal
    :key="comment?.comment.metadata.name"
    v-model:visible="replyModal"
    :comment="comment"
    :reply="selectedReply"
    @close="onReplyCreationModalClose"
  />
  <VEntity :is-selected="isSelected" :class="{ 'hover:bg-white': showReplies }">
    <template v-if="showReplies" #prepend>
      <div class="absolute inset-y-0 left-0 w-[1px] bg-black/50"></div>
      <div class="absolute inset-y-0 right-0 w-[1px] bg-black/50"></div>
      <div class="absolute inset-x-0 top-0 h-[1px] bg-black/50"></div>
      <div class="absolute inset-x-0 bottom-0 h-[1px] bg-black/50"></div>
    </template>
    <template
      v-if="currentUserHasPermission(['system:comments:manage'])"
      #checkbox
    >
      <slot name="checkbox" />
    </template>
    <template #start>
      <VEntityField>
        <template #description>
          <VAvatar
            circle
            :src="comment?.owner.avatar"
            :alt="comment?.owner.displayName"
            size="md"
          ></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        class="w-28 min-w-[7rem]"
        :title="comment?.owner?.displayName"
        :description="comment?.owner?.email"
      ></VEntityField>
      <VEntityField width="100%">
        <template #description>
          <div class="flex flex-col gap-2">
            <div class="mb-1 flex items-center gap-2">
              <VTag>{{ subjectRefResult.label }}</VTag>
              <RouterLink
                :to="subjectRefResult.route || $route"
                class="truncate text-sm font-medium text-gray-900 hover:text-gray-600"
              >
                {{ subjectRefResult.title }}
              </RouterLink>
              <a
                v-if="subjectRefResult.externalUrl"
                :href="subjectRefResult.externalUrl"
                target="_blank"
                class="hidden text-gray-600 hover:text-gray-900 group-hover:block"
              >
                <IconExternalLinkLine class="h-3.5 w-3.5" />
              </a>
            </div>
            <div class="break-all text-sm text-gray-900">
              {{ comment?.comment?.spec.content }}
            </div>
            <div class="flex items-center gap-3 text-xs">
              <span
                class="select-none text-gray-700 hover:text-gray-900"
                @click="handleToggleShowReplies"
              >
                {{
                  $t("core.comment.list.fields.reply_count", {
                    count: comment?.comment?.status?.replyCount || 0,
                  })
                }}
              </span>
              <VStatusDot
                v-if="comment?.comment?.status?.unreadReplyCount || 0 > 0"
                v-tooltip="$t('core.comment.list.fields.has_new_replies')"
                state="success"
                animate
              />
              <span
                class="select-none text-gray-700 hover:text-gray-900"
                @click="handleTriggerReply"
              >
                {{ $t("core.comment.operations.reply.button") }}
              </span>
            </div>
          </div>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="!comment?.comment.spec.approved">
        <template #description>
          <VStatusDot state="success">
            <template #text>
              <span class="text-xs text-gray-500">
                {{ $t("core.comment.list.fields.pending_review") }}
              </span>
            </template>
          </VStatusDot>
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
      <VEntityField>
        <template #description>
          <span class="truncate text-xs tabular-nums text-gray-500">
            {{
              formatDatetime(
                comment?.comment.spec.creationTime ||
                  comment?.comment.metadata.creationTimestamp
              )
            }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template
      v-if="currentUserHasPermission(['system:comments:manage'])"
      #dropdownItems
    >
      <VDropdownItem
        v-if="!comment?.comment.spec.approved"
        @click="handleApprove"
      >
        {{ $t("core.comment.operations.approve_comment_in_batch.button") }}
      </VDropdownItem>
      <VDropdownItem @click="handleApproveReplyInBatch">
        {{ $t("core.comment.operations.approve_applies_in_batch.button") }}
      </VDropdownItem>
      <VDropdownItem type="danger" @click="handleDelete">
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>

    <template v-if="showReplies" #footer>
      <!-- Replies -->
      <div
        class="ml-8 mt-3 divide-y divide-gray-100 rounded-base border-t border-gray-100 pt-3"
      >
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
                <VButton type="secondary" @click="replyModal = true">
                  <template #icon>
                    <IconAddCircle class="h-full w-full" />
                  </template>
                  {{ $t("core.comment.reply_empty.new") }}
                </VButton>
              </VSpace>
            </template>
          </VEmpty>
        </Transition>
        <Transition v-else appear name="fade">
          <div>
            <ReplyListItem
              v-for="reply in replies"
              :key="reply.reply.metadata.name"
              :class="{ 'hover:bg-white': showReplies }"
              :reply="reply"
              :replies="replies"
              @reload="refetch()"
              @reply="onTriggerReply"
            ></ReplyListItem>
          </div>
        </Transition>
      </div>
    </template>
  </VEntity>
</template>
