<script lang="ts" setup>
import {
  VAvatar,
  VButton,
  VTag,
  VEntityField,
  VEntity,
  useDialog,
  VStatusDot,
  IconReplyLine,
} from "@halo-dev/components";
import type { ListedReply } from "@halo-dev/api-client";
import { formatDatetime } from "@/utils/date";
import { apiClient } from "@halo-dev/admin-shared";
import { computed, inject, type Ref } from "vue";
import cloneDeep from "lodash.clonedeep";

const props = withDefaults(
  defineProps<{
    reply?: ListedReply;
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

const dialog = useDialog();

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
  dialog.warning({
    title: "是否确认删除该回复？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.reply.deletecontentHaloRunV1alpha1Reply({
          name: props.reply?.reply.metadata.name as string,
        });
      } catch (error) {
        console.log("Failed to delete comment reply", error);
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
    await apiClient.extension.reply.updatecontentHaloRunV1alpha1Reply({
      name: replyToUpdate.metadata.name,
      reply: replyToUpdate,
    });
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
          <VAvatar circle :src="reply?.owner.avatar" size="md"></VAvatar>
        </template>
      </VEntityField>
      <VEntityField
        class="w-28 min-w-[7rem]"
        :title="reply?.owner.displayName"
        :description="reply?.owner.email"
      ></VEntityField>
      <VEntityField>
        <template #description>
          <div class="flex flex-col gap-2">
            <div class="w-96 text-sm text-gray-800">
              <p>
                <a
                  v-if="quoteReply"
                  class="mr-1 inline-flex flex-row items-center gap-1 rounded bg-gray-200 py-0.5 px-1 text-xs font-medium text-gray-600 hover:text-blue-500 hover:underline"
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
                回复
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
              <span class="text-xs text-gray-500">待审核</span>
            </template>
          </VStatusDot>
        </template>
      </VEntityField>
      <VEntityField v-if="reply?.reply.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot v-tooltip="`删除中`" state="warning" animate />
        </template>
      </VEntityField>
      <VEntityField
        :description="formatDatetime(reply?.reply.metadata.creationTimestamp)"
      >
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VButton
        v-if="!reply?.reply.spec.approved"
        v-close-popper
        type="secondary"
        block
        @click="handleApprove"
      >
        审核通过
      </VButton>
      <VButton v-close-popper block type="danger" @click="handleDelete">
        删除
      </VButton>
    </template>
  </VEntity>
</template>
