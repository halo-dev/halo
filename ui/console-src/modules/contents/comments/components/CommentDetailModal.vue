<script setup lang="ts">
import {
  consoleApiClient,
  coreApiClient,
  type ListedComment,
} from "@halo-dev/api-client";
import {
  IconExternalLinkLine,
  Toast,
  VButton,
  VDescription,
  VDescriptionItem,
  VModal,
  VSpace,
  VTag,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { useUserAgent } from "@uc/modules/profile/tabs/composables/use-user-agent";
import { computed, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import { useContentProviderExtensionPoint } from "../composables/use-content-provider-extension-point";
import { useSubjectRef } from "../composables/use-subject-ref";
import CommentEditor from "./CommentEditor.vue";
import OwnerButton from "./OwnerButton.vue";

const props = withDefaults(
  defineProps<{
    comment: ListedComment;
  }>(),
  {}
);

const queryClient = useQueryClient();
const { t } = useI18n();

const emit = defineEmits<{
  (e: "close"): void;
}>();

const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");

const { os, browser } = useUserAgent(props.comment.comment.spec.userAgent);

const creationTime = computed(() => {
  return (
    props.comment?.comment.spec.creationTime ||
    props.comment?.comment.metadata.creationTimestamp
  );
});

const editorContent = ref("");
const editorCharacterCount = ref(0);

function onCommentEditorUpdate(value: {
  content: string;
  characterCount: number;
}) {
  editorContent.value = value.content;
  editorCharacterCount.value = value.characterCount;
}

async function handleApprove() {
  if (!editorCharacterCount.value) {
    await coreApiClient.content.comment.patchComment({
      name: props.comment.comment.metadata.name,
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
        raw: editorContent.value,
        content: editorContent.value,
        allowNotification: true,
        quoteReply: undefined,
      },
    });
  }
  modal.value?.close();
  queryClient.invalidateQueries({ queryKey: ["core:comments"] });
  Toast.success(t("core.common.toast.operation_success"));
}

const { subjectRefResult } = useSubjectRef(props.comment);

const websiteOfAnonymous = computed(() => {
  return props.comment.comment.spec.owner.annotations?.["website"];
});

const { data: contentProvider } = useContentProviderExtensionPoint();
</script>
<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    :width="900"
    :title="$t('core.comment.comment_detail_modal.title')"
    mount-to-body
    :centered="false"
    @close="emit('close')"
  >
    <div>
      <VDescription>
        <VDescriptionItem :label="$t('core.comment.detail_modal.fields.owner')">
          <div class="flex items-center gap-3">
            <OwnerButton
              v-if="comment.comment.spec.owner.kind === 'User'"
              :owner="comment.comment.spec.owner"
              @click="
                $router.push({
                  name: 'UserDetail',
                  params: { name: comment.comment.spec.owner.name },
                })
              "
            />
            <ul v-else class="space-y-1">
              <li>{{ comment.comment.spec.owner.displayName }}</li>
              <li>{{ comment.comment.spec.owner.name }}</li>
              <li v-if="websiteOfAnonymous">
                <a :href="websiteOfAnonymous" target="_blank">{{
                  websiteOfAnonymous
                }}</a>
              </li>
            </ul>
          </div>
        </VDescriptionItem>
        <VDescriptionItem label="IP">
          {{ comment.comment.spec.ipAddress }}
        </VDescriptionItem>
        <VDescriptionItem
          :label="$t('core.comment.detail_modal.fields.user_agent')"
        >
          <span v-tooltip="comment.comment.spec.userAgent">
            {{ os }} {{ browser }}
          </span>
        </VDescriptionItem>
        <VDescriptionItem
          :label="$t('core.comment.detail_modal.fields.creation_time')"
        >
          <span v-tooltip="utils.date.format(creationTime)">
            {{ utils.date.timeAgo(creationTime) }}
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
          :label="$t('core.comment.comment_detail_modal.fields.content')"
        >
          <div v-if="comment.comment.spec.hidden" class="mb-2">
            <VTag>
              {{ $t("core.comment.list.fields.private") }}
            </VTag>
          </div>
          <component
            :is="contentProvider?.component"
            :content="comment.comment.spec.content"
          />
        </VDescriptionItem>
        <HasPermission :permissions="['system:comments:manage']">
          <VDescriptionItem
            v-if="!comment.comment.spec.approved"
            :label="$t('core.comment.detail_modal.fields.new_reply')"
          >
            <CommentEditor @update="onCommentEditorUpdate" />
          </VDescriptionItem>
        </HasPermission>
      </VDescription>
    </div>
    <template #footer>
      <VSpace>
        <HasPermission :permissions="['system:comments:manage']">
          <VButton
            v-if="!comment.comment.spec.approved"
            type="secondary"
            @click="handleApprove"
          >
            {{
              editorCharacterCount > 0
                ? $t("core.comment.operations.reply_and_approve.button")
                : $t("core.comment.operations.approve.button")
            }}
          </VButton>
        </HasPermission>
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
