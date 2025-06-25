<script setup lang="ts">
import { formatDatetime, relativeTimeTo } from "@/utils/date";
import {
  consoleApiClient,
  coreApiClient,
  type ListedComment,
  type ListedReply,
} from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  IconReplyLine,
  Toast,
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { useUserAgent } from "@uc/modules/profile/tabs/composables/use-user-agent";
import { computed, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import { useSubjectRef } from "../composables/use-subject-ref";
import OwnerButton from "./OwnerButton.vue";
import ReplyFormItems from "./ReplyFormItems.vue";

const props = withDefaults(
  defineProps<{
    comment: ListedComment;
    reply: ListedReply;
    quoteReply?: ListedReply;
  }>(),
  {
    quoteReply: undefined,
  }
);

const queryClient = useQueryClient();
const { t } = useI18n();

const emit = defineEmits<{
  (e: "close"): void;
}>();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const { os, browser } = useUserAgent(props.reply.reply.spec.userAgent);

const creationTime = computed(() => {
  return (
    props.reply?.reply.spec.creationTime ||
    props.reply?.reply.metadata.creationTimestamp
  );
});

const newReply = ref("");

async function handleApprove() {
  if (!newReply.value) {
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
          value: new Date().toISOString(),
        },
      ],
    });
  } else {
    await consoleApiClient.content.comment.createReply({
      name: props.comment?.comment.metadata.name as string,
      replyRequest: {
        raw: newReply.value,
        content: newReply.value,
        allowNotification: true,
        quoteReply: props.reply.reply.metadata.name,
      },
    });
  }
  modal.value?.close();
  queryClient.invalidateQueries({
    queryKey: ["core:comment-replies", props.comment.comment.metadata.name],
  });
  Toast.success(t("core.common.toast.operation_success"));
}

const { subjectRefResult } = useSubjectRef(props.comment);

const websiteOfAnonymous = computed(() => {
  return props.reply.reply.spec.owner.annotations?.["website"];
});
</script>
<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :width="900"
    :title="$t('core.comment.reply_detail_modal.title')"
    mount-to-body
    :centered="false"
    @close="emit('close')"
  >
    <div>
      <VDescription>
        <VDescriptionItem :label="$t('core.comment.detail_modal.fields.owner')">
          <div class="flex items-center gap-3">
            <OwnerButton
              v-if="reply.owner.kind === 'User'"
              :owner="reply.owner"
              @click="
                $router.push({
                  name: 'UserDetail',
                  params: { name: reply.owner.name },
                })
              "
            />
            <ul v-else class="space-y-1">
              <li>{{ reply.owner.displayName }}</li>
              <li>{{ reply.owner.name }}</li>
              <li v-if="websiteOfAnonymous">
                <a :href="websiteOfAnonymous" target="_blank">{{
                  websiteOfAnonymous
                }}</a>
              </li>
            </ul>
          </div>
        </VDescriptionItem>
        <VDescriptionItem label="IP">
          {{ reply.reply.spec.ipAddress }}
        </VDescriptionItem>
        <VDescriptionItem
          :label="$t('core.comment.detail_modal.fields.user_agent')"
        >
          <span v-tooltip="reply.reply.spec.userAgent">
            {{ os }} {{ browser }}
          </span>
        </VDescriptionItem>
        <VDescriptionItem
          :label="$t('core.comment.detail_modal.fields.creation_time')"
        >
          <span v-tooltip="formatDatetime(creationTime)">
            {{ relativeTimeTo(creationTime) }}
          </span>
        </VDescriptionItem>
        <VDescriptionItem
          :label="$t('core.comment.detail_modal.fields.commented_on')"
        >
          <div class="flex items-center gap-2">
            <RouterLink
              v-tooltip="`${subjectRefResult.label}`"
              :to="subjectRefResult.route || $route"
              class="inline-block text-sm hover:text-gray-600"
            >
              {{ subjectRefResult.title }}
            </RouterLink>
            <a
              v-if="subjectRefResult.externalUrl"
              :href="subjectRefResult.externalUrl"
              target="_blank"
              class="text-gray-600 hover:text-gray-900"
            >
              <IconExternalLinkLine class="h-3.5 w-3.5" />
            </a>
          </div>
        </VDescriptionItem>
        <VDescriptionItem
          :label="$t('core.comment.reply_detail_modal.fields.original_comment')"
        >
          <OwnerButton :owner="comment.owner" />
          <pre
            class="mt-2 whitespace-pre-wrap break-words text-sm text-gray-900"
            >{{ comment.comment.spec.content }}</pre
          >
        </VDescriptionItem>
        <VDescriptionItem
          :label="$t('core.comment.reply_detail_modal.fields.content')"
        >
          <pre
            class="whitespace-pre-wrap break-words text-sm text-gray-900"
          ><span
                  v-if="quoteReply"
                  v-tooltip="`${quoteReply.owner.displayName}: ${quoteReply.reply.spec.content}`"
                  class="mr-1 inline-flex cursor-pointer flex-row items-center gap-1 rounded bg-slate-100 px-1 py-0.5 text-xs font-medium text-slate-700 hover:bg-slate-200 hover:text-slate-800 hover:underline"
                >
                  <IconReplyLine />
                  <span>{{ quoteReply.owner.displayName }}</span>
                </span><br v-if="quoteReply" />{{ reply?.reply.spec.content }}</pre>
        </VDescriptionItem>
        <VDescriptionItem
          v-if="!reply.reply.spec.approved"
          :label="$t('core.comment.detail_modal.fields.new_reply')"
        >
          <ReplyFormItems
            :required="false"
            :auto-focus="false"
            @update="newReply = $event"
          />
        </VDescriptionItem>
      </VDescription>
    </div>
    <template #footer>
      <VSpace>
        <VButton
          v-if="!reply.reply.spec.approved"
          type="secondary"
          @click="handleApprove"
        >
          {{
            newReply
              ? $t("core.comment.operations.reply_and_approve.button")
              : $t("core.comment.operations.approve.button")
          }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<style scoped>
:deep(.description-item__content) {
  @apply lg:col-span-5;
}
</style>
