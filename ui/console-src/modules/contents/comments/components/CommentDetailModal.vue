<script setup lang="ts">
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import type { ListedComment } from "@halo-dev/api-client";
import {
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { useUserAgent } from "@uc/modules/profile/tabs/composables/use-user-agent";
import { computed, ref, useTemplateRef } from "vue";
import OwnerButton from "./OwnerButton.vue";

const props = withDefaults(
  defineProps<{
    comment: ListedComment;
  }>(),
  {}
);

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const { os, browser } = useUserAgent(props.comment.comment.spec.userAgent);

const creationTime = computed(() => {
  return (
    props.comment?.comment.spec.creationTime ||
    props.comment?.comment.metadata.creationTimestamp
  );
});

const newReply = ref("");
</script>
<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :width="900"
    title="Comment detail"
    mount-to-body
    :centered="false"
  >
    <VDescription>
      <VDescriptionItem label="Owner">
        <div class="flex items-center gap-3">
          <OwnerButton :owner="comment.owner" />
          <VTag v-if="comment.owner.kind === 'Email'">Anonymous</VTag>
        </div>
      </VDescriptionItem>
      <VDescriptionItem label="IP">
        {{ comment.comment.spec.ipAddress }}
      </VDescriptionItem>
      <VDescriptionItem label="User agent">
        <span v-tooltip="comment.comment.spec.userAgent">
          {{ os }} {{ browser }}
        </span>
      </VDescriptionItem>
      <VDescriptionItem label="Created at">
        <span v-tooltip="formatDatetime(creationTime)">
          {{ relativeTimeTo(creationTime) }}
        </span>
      </VDescriptionItem>
      <VDescriptionItem label="Content">
        <pre
          class="whitespace-pre-wrap break-words break-all text-sm text-gray-900"
          >{{ comment.comment.spec.content }}</pre
        >
      </VDescriptionItem>
      <VDescriptionItem v-if="!comment.comment.spec.approved" label="New reply">
        <FormKit v-model="newReply" type="textarea"></FormKit>
      </VDescriptionItem>
    </VDescription>
    <template #footer>
      <VSpace>
        <VButton v-if="!comment.comment.spec.approved" type="secondary">
          {{ newReply ? "Reply and approve" : "Approve" }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
