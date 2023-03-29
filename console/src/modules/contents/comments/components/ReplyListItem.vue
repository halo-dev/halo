<script lang="ts" setup>
import {
  VAvatar,
  VTag,
  VEntityField,
  VEntity,
  Dialog,
  VStatusDot,
  VDropdownItem,
  IconReplyLine,
  Toast,
} from "@halo-dev/components";
import type { ListedReply } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { apiClient } from "@/utils/api-client";
import { computed, inject, type Ref } from "vue";
import cloneDeep from "lodash.clonedeep";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    reply: ListedReply;
    replies?: ListedReply[];
  }>(),
  {
    reply: undefined,
    replies: undefined,
  }
);

const emit = defineEmits<{
  (event: "reload"): void;
  (event: "reply", reply: ListedReply): void;
}>();

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
        await apiClient.extension.reply.deletecontentHaloRunV1alpha1Reply({
          name: props.reply?.reply.metadata.name as string,
        });

        Toast.success(t("core.common.toast.delete_success"));
      } catch (error) {
        console.error("Failed to delete comment reply", error);
      } finally {
        emit("reload");
      }
    },
  });
};

const handleApprove = async () => {
  try {
    const replyToUpdate = cloneDeep(props.reply.reply);
    replyToUpdate.spec.approved = true;
    // TODO: 暂时由前端设置发布时间。see https://github.com/halo-dev/halo/pull/2746
    replyToUpdate.spec.approvedTime = new Date().toISOString();
    await apiClient.extension.reply.updatecontentHaloRunV1alpha1Reply({
      name: replyToUpdate.metadata.name,
      reply: replyToUpdate,
    });

    Toast.success(t("core.common.toast.operation_success"));
  } catch (error) {
    console.error("Failed to approve comment reply", error);
  } finally {
    emit("reload");
  }
};

const handleTriggerReply = () => {
  emit("reply", props.reply);
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
</script>

<template>
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
                @click="handleTriggerReply"
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
      <VDropdownItem
        v-if="!reply?.reply.spec.approved"
        v-permission="['system:comments:manage']"
        @click="handleApprove"
      >
        {{ $t("core.comment.operations.approve_reply.button") }}
      </VDropdownItem>
      <VDropdownItem
        v-permission="['system:comments:manage']"
        type="danger"
        @click="handleDelete"
      >
        {{ $t("core.common.buttons.delete") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
