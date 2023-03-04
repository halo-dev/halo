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
import { computed, provide, ref, watch, type Ref } from "vue";
import ReplyListItem from "./ReplyListItem.vue";
import { apiClient } from "@/utils/api-client";
import type { RouteLocationRaw } from "vue-router";
import cloneDeep from "lodash.clonedeep";
import { usePermission } from "@/utils/permission";
import { useQuery } from "@tanstack/vue-query";

const { currentUserHasPermission } = usePermission();

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
    title: "确认要删除该评论吗？",
    description: "删除评论的同时会删除该评论下的所有回复，该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.comment.deletecontentHaloRunV1alpha1Comment({
          name: props.comment?.comment?.metadata.name as string,
        });

        Toast.success("删除成功");
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
    title: "确定要审核通过该评论的所有回复吗？",
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

        Toast.success("操作成功");
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

    Toast.success("操作成功");
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
  refetchOnWindowFocus: false,
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
        label: "文章",
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
        label: "单页",
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
      label: "未知",
      title: "未知",
    };
  }
  const subjectRef = SubjectRefProvider.value.find((provider) =>
    Object.keys(provider).includes(subject.kind)
  );
  if (!subjectRef) {
    return {
      label: "未知",
      title: "未知",
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
      <VEntityField width="60%">
        <template #description>
          <div class="flex flex-col gap-2">
            <div class="break-all text-sm text-gray-900">
              {{ comment?.comment?.spec.content }}
            </div>
            <div class="flex items-center gap-3 text-xs">
              <span
                class="select-none text-gray-700 hover:text-gray-900"
                @click="handleToggleShowReplies"
              >
                {{ comment?.comment?.status?.replyCount || 0 }} 条回复
              </span>
              <VStatusDot
                v-if="comment?.comment?.status?.unreadReplyCount || 0 > 0"
                v-tooltip="`有新的回复`"
                state="success"
                animate
              />
              <span
                class="select-none text-gray-700 hover:text-gray-900"
                @click="handleTriggerReply"
              >
                回复
              </span>
            </div>
          </div>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField
        :title="subjectRefResult.title"
        :description="subjectRefResult.label"
        :route="subjectRefResult.route"
      >
        <template #extra>
          <a
            v-if="subjectRefResult.externalUrl"
            :href="subjectRefResult.externalUrl"
            target="_blank"
            class="text-gray-600 hover:text-gray-900"
          >
            <IconExternalLinkLine class="h-3.5 w-3.5" />
          </a>
        </template>
      </VEntityField>
      <VEntityField v-if="!comment?.comment.spec.approved">
        <template #description>
          <VStatusDot state="success">
            <template #text>
              <span class="text-xs text-gray-500">待审核</span>
            </template>
          </VStatusDot>
        </template>
      </VEntityField>
      <VEntityField v-if="comment?.comment?.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot v-tooltip="`删除中`" state="warning" animate />
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
      <VButton
        v-if="!comment?.comment.spec.approved"
        v-close-popper
        type="secondary"
        block
        @click="handleApprove"
      >
        审核通过
      </VButton>
      <VButton
        v-close-popper
        type="secondary"
        block
        @click="handleApproveReplyInBatch"
      >
        审核通过所有回复
      </VButton>
      <VButton v-close-popper block type="danger" @click="handleDelete">
        删除
      </VButton>
    </template>

    <template v-if="showReplies" #footer>
      <!-- Replies -->
      <div
        class="ml-8 mt-3 divide-y divide-gray-100 rounded-base border-t border-gray-100 pt-3"
      >
        <VLoading v-if="isLoading" />
        <Transition v-else-if="!replies?.length" appear name="fade">
          <VEmpty message="你可以尝试刷新或者创建新回复" title="当前没有回复">
            <template #actions>
              <VSpace>
                <VButton @click="refetch()">刷新</VButton>
                <VButton type="secondary" @click="replyModal = true">
                  <template #icon>
                    <IconAddCircle class="h-full w-full" />
                  </template>
                  创建新回复
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
