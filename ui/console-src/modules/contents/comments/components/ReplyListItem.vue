<script lang="ts" setup>
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import { formatDatetime } from "@/utils/date";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import type { ListedComment, ListedReply } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconReplyLine,
  Toast,
  VAvatar,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import type { OperationItem } from "@halo-dev/console-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { computed, inject, markRaw, ref, type Ref, toRefs } from "vue";
import { useI18n } from "vue-i18n";
import ReplyCreationModal from "./ReplyCreationModal.vue";

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
        queryClient.invalidateQueries({ queryKey: ["comment-replies"] });
      }
    },
  });
};

const handleApprove = async () => {
  try {
    await coreApiClient.content.reply.patchReply({
      name: props.reply.reply.metadata.name,
      jsonPatchInner: [
        {
          op: "add",
          path: "/spec/approved",
          value: true,
        },
        {
          op: "add",
          path: "/spec/approvedTime",
          // TODO: 暂时由前端设置发布时间。see https://github.com/halo-dev/halo/pull/2746
          value: new Date().toISOString(),
        },
      ],
    });

    Toast.success(t("core.common.toast.operation_success"));
  } catch (error) {
    console.error("Failed to approve comment reply", error);
  } finally {
    queryClient.invalidateQueries({ queryKey: ["comment-replies"] });
  }
};

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

function onReplyCreationModalClose() {
  queryClient.invalidateQueries({
    queryKey: ["comment-replies", props.comment.comment.metadata.name],
  });
  replyModal.value = false;
}

const { operationItems } = useOperationItemExtensionPoint<ListedReply>(
  "reply:list-item:operation:create",
  reply,
  computed((): OperationItem<ListedReply>[] => [
    {
      priority: 0,
      component: markRaw(VDropdownItem),
      label: t("core.comment.operations.approve_reply.button"),
      permissions: ["system:comments:manage"],
      action: handleApprove,
      hidden: props.reply?.reply.spec.approved,
    },
    {
      priority: 10,
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
</script>

<template>
  <ReplyCreationModal
    v-if="replyModal"
    :comment="comment"
    :reply="reply"
    @close="onReplyCreationModalClose"
  />
  <VEntity class="!px-0 !py-2" :class="{ 'animate-breath': isHoveredReply }">
    <template #start>
      <VEntityField>
        <template #description>
          <VAvatar
            circle
            :src="reply?.owner.avatar"
            :alt="reply?.owner.displayName"
            size="md"
          ></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        class="w-28 min-w-[7rem]"
        :title="reply?.owner.displayName"
        :description="reply?.owner.email"
      ></VEntityField>
      <VEntityField width="60%">
        <template #description>
          <div class="flex flex-col gap-2">
            <div class="text-sm text-gray-800">
              <p class="break-all">
                <a
                  v-if="quoteReply"
                  class="mr-1 inline-flex flex-row items-center gap-1 rounded bg-gray-200 px-1 py-0.5 text-xs font-medium text-gray-600 hover:text-blue-500 hover:underline"
                  href="javascript:void(0)"
                  @mouseenter="handleShowQuoteReply(true)"
                  @mouseleave="handleShowQuoteReply(false)"
                >
                  <IconReplyLine />
                  <span>{{ quoteReply.owner.displayName }}</span>
                </a>
                <br v-if="quoteReply" />
                {{ reply?.reply.spec.content }}
              </p>
            </div>
            <div class="flex items-center gap-3 text-xs">
              <span
                class="select-none text-gray-700 hover:text-gray-900"
                @click="replyModal = true"
              >
                {{ $t("core.comment.operations.reply.button") }}
              </span>
              <div v-if="false" class="flex items-center">
                <VTag>New</VTag>
              </div>
            </div>
          </div>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="!reply?.reply.spec.approved">
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
      <VEntityField v-if="reply?.reply.metadata.deletionTimestamp">
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
                reply?.reply?.spec.creationTime ||
                  reply?.reply.metadata.creationTimestamp
              )
            }}
          </span>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <EntityDropdownItems :dropdown-items="operationItems" :item="reply" />
    </template>
  </VEntity>
</template>
