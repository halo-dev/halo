<script lang="ts" setup>
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconReplyLine,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { utils, type OperationItem } from "@halo-dev/ui-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { computed, inject, markRaw, ref, toRefs, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import { useCommentLastReadTimeMutate } from "../composables/use-comment-last-readtime-mutate";
import { useContentProviderExtensionPoint } from "../composables/use-content-provider-extension-point";
import OwnerButton from "./OwnerButton.vue";
import ReplyCreationModal from "./ReplyCreationModal.vue";
import ReplyDetailModal from "./ReplyDetailModal.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    comment: ListedComment;
    reply: ListedReply;
    replies?: ListedReply[];
  }>(),
  {
    reply: undefined,
    replies: undefined,
  }
);

const { reply } = toRefs(props);

const creationTime = computed(() => {
  return (
    props.reply?.reply.spec.creationTime ||
    props.reply?.reply.metadata.creationTimestamp
  );
});

const quoteReply = computed(() => {
  const { quoteReply: replyName } = props.reply.reply.spec;

  if (!replyName) {
    return undefined;
  }

  return props.replies?.find(
    (reply) => reply.reply.metadata.name === replyName
  );
});

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.comment.operations.delete_reply.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await coreApiClient.content.reply.deleteReply({
          name: props.reply?.reply.metadata.name as string,
        });

        Toast.success(t("core.common.toast.delete_success"));
      } catch (error) {
        console.error("Failed to delete comment reply", error);
      } finally {
        queryClient.invalidateQueries({
          queryKey: [
            "core:comment-replies",
            props.comment.comment.metadata.name,
          ],
        });
      }
    },
  });
};

async function handleCancelApprove() {
  await coreApiClient.content.reply.patchReply({
    name: props.reply?.reply.metadata.name as string,
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
  queryClient.invalidateQueries({
    queryKey: ["core:comment-replies", props.comment.comment.metadata.name],
  });
}

// Show hovered reply
const hoveredReply = inject<Ref<ListedReply | undefined>>("hoveredReply");

const handleShowQuoteReply = (show: boolean) => {
  if (hoveredReply) {
    hoveredReply.value = show ? quoteReply.value : undefined;
  }
};

const isHoveredReply = computed(() => {
  return (
    hoveredReply?.value?.reply.metadata.name === props.reply.reply.metadata.name
  );
});

// Create reply
const replyModal = ref(false);
const detailModalVisible = ref(false);

const { mutate: updateCommentLastReadTimeMutate } =
  useCommentLastReadTimeMutate(props.comment);

function onReplyCreationModalClose() {
  queryClient.invalidateQueries({
    queryKey: ["core:comment-replies", props.comment.comment.metadata.name],
  });
  queryClient.invalidateQueries({ queryKey: ["core:comments"] });
  updateCommentLastReadTimeMutate();
  replyModal.value = false;
  detailModalVisible.value = false;
}

const { data: operationItems } = useOperationItemExtensionPoint<ListedReply>(
  "reply:list-item:operation:create",
  reply,
  computed((): OperationItem<ListedReply>[] => [
    {
      priority: 0,
      component: markRaw(VDropdownItem),
      label: t("core.comment.operations.review.button"),
      permissions: ["system:comments:manage"],
      action: () => {
        detailModalVisible.value = true;
      },
      hidden: props.reply?.reply.spec.approved,
    },
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.text.detail"),
      hidden: !props.reply?.reply.spec.approved,
      action: () => {
        detailModalVisible.value = true;
      },
    },
    {
      priority: 20,
      component: markRaw(VDropdownDivider),
    },
    {
      priority: 30,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.comment.operations.cancel_approve.button"),
      hidden: !props.reply?.reply.spec.approved,
      action: handleCancelApprove,
    },
    {
      priority: 40,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.delete"),
      permissions: ["system:comments:manage"],
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
    :reply="reply"
    @close="onReplyCreationModalClose"
  />
  <ReplyDetailModal
    v-if="detailModalVisible"
    :comment="comment"
    :reply="reply"
    :quote-reply="quoteReply"
    @close="onReplyCreationModalClose"
  />
  <VEntity
    v-bind="$attrs"
    class="border-l border-dashed !border-gray-200"
    :class="{ 'animate-breath': isHoveredReply }"
  >
    <template #start>
      <VEntityField width="100%" max-width="100%">
        <template #description>
          <div class="flex flex-col gap-2">
            <div class="mb-1 flex items-center gap-2">
              <OwnerButton
                :owner="reply?.owner"
                @click="detailModalVisible = true"
              />
              <VTag v-if="comment.comment.spec.hidden">
                {{ $t("core.comment.list.fields.private") }}
              </VTag>
              <span class="whitespace-nowrap text-sm text-gray-900">
                {{ $t("core.comment.text.replied_below") }}
              </span>
            </div>
            <div>
              <a
                v-if="quoteReply"
                class="mr-1 inline-flex flex-row items-center gap-1 rounded bg-slate-100 px-1 py-0.5 text-xs font-medium text-slate-700 hover:bg-slate-200 hover:text-slate-800 hover:underline"
                href="javascript:void(0)"
                @mouseenter="handleShowQuoteReply(true)"
                @mouseleave="handleShowQuoteReply(false)"
              >
                <IconReplyLine />
                <span>{{ quoteReply.owner.displayName }}</span> </a
              ><br v-if="quoteReply" /><component
                :is="contentProvider?.component"
                :content="reply?.reply.spec.content"
              />
            </div>
            <HasPermission :permissions="['system:comments:manage']">
              <div class="flex items-center gap-3 text-xs">
                <span
                  class="cursor-pointer select-none text-gray-700 hover:text-gray-900"
                  @click="replyModal = true"
                >
                  {{ $t("core.comment.operations.reply.button") }}
                </span>
              </div>
            </HasPermission>
          </div>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="!reply?.reply.spec.approved">
        <template #description>
          <VStatusDot
            state="warning"
            animate
            :text="$t('core.comment.list.fields.pending_review')"
          />
        </template>
      </VEntityField>
      <VEntityField v-if="reply?.reply.metadata.deletionTimestamp">
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
        :item="reply"
      />
    </template>
  </VEntity>
</template>
