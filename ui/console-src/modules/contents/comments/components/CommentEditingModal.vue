<script lang="ts" setup generic="T extends Comment | Reply">
import {
  consoleApiClient,
  coreApiClient,
  type Comment,
  type Reply,
} from "@halo-dev/api-client";
import { Toast, VButton, VLoading, VModal, VSpace } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQueryClient } from "@tanstack/vue-query";
import { isAxiosError } from "axios";
import { computed, onMounted, ref, shallowRef, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import SubmitButton from "@/components/button/SubmitButton.vue";
import CommentEditor from "./CommentEditor.vue";

const props = defineProps<{
  target: T;
}>();
const emit = defineEmits<{ (event: "close"): void }>();
const { t } = useI18n();
const queryClient = useQueryClient();
const modal = useTemplateRef<InstanceType<typeof VModal>>("modal");

const target = props.target;
const name = target.metadata.name;
const latestTarget = shallowRef<Comment | Reply>();
const version = computed(() => latestTarget.value?.metadata.version);
const commentName =
  "commentName" in target.spec ? target.spec.commentName : name;
const isReply = "commentName" in target.spec;
const initialContent = computed(() => latestTarget.value?.spec.raw ?? "");
const content = ref("");
const isLoading = ref(true);
const isSubmitting = ref(false);
const errorMessage = ref("");

onMounted(async () => {
  try {
    const { data } = isReply
      ? await coreApiClient.content.reply.getReply({ name })
      : await coreApiClient.content.comment.getComment({ name });
    if (data.metadata.deletionTimestamp) {
      errorMessage.value = t("core.common.status.deleting");
      return;
    }
    latestTarget.value = data;
    content.value = data.spec.raw;
  } catch {
    errorMessage.value = t("core.common.status.loading_error");
  } finally {
    isLoading.value = false;
  }
});

const canSubmit = computed(() => {
  if (
    isLoading.value ||
    isSubmitting.value ||
    content.value === initialContent.value ||
    version.value === undefined ||
    !utils.permission.has(["system:comments:manage"])
  ) {
    return false;
  }
  const body = new DOMParser().parseFromString(content.value, "text/html").body;
  return (
    !!body.textContent?.replaceAll("\u00a0", " ").trim() ||
    Array.from(body.querySelectorAll("img[src]")).some((image) =>
      image.getAttribute("src")?.trim()
    )
  );
});

async function handleSubmit() {
  if (!canSubmit.value) {
    return;
  }
  isSubmitting.value = true;
  errorMessage.value = "";
  try {
    const request = {
      name,
      commentContentRequest: {
        raw: content.value,
        content: content.value,
        version: version.value!,
      },
    };
    if (isReply) {
      await consoleApiClient.content.reply.updateReplyContent(request);
    } else {
      await consoleApiClient.content.comment.updateCommentContent(request);
    }
    queryClient.invalidateQueries({ queryKey: ["core:comments"] });
    queryClient.invalidateQueries({ queryKey: ["core:comments:with-subject"] });
    queryClient.invalidateQueries({ queryKey: ["widget-pending-comments"] });
    queryClient.invalidateQueries({
      queryKey: ["core:comment-replies", commentName],
    });
    Toast.success(t("core.common.toast.save_success"));
    modal.value?.close();
  } catch (error) {
    errorMessage.value = t(
      isAxiosError(error) && error.response?.status === 409
        ? "core.comment.edit_modal.conflict"
        : "core.comment.edit_modal.save_failed"
    );
    isSubmitting.value = false;
  }
}
</script>

<template>
  <VModal
    ref="modal"
    :title="
      $t(
        isReply
          ? 'core.comment.edit_modal.reply_title'
          : 'core.comment.edit_modal.title'
      )
    "
    :width="600"
    mount-to-body
    :centered="false"
    @close="emit('close')"
  >
    <div>
      <VLoading v-if="isLoading" />
      <CommentEditor
        v-else-if="latestTarget"
        :initial-content="initialContent"
        auto-focus
        @update="content = $event.content"
      />
      <p v-if="errorMessage" role="alert" class="mt-3 text-sm text-red-500">
        {{ errorMessage }}
      </p>
    </div>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isSubmitting"
          :disabled="!canSubmit"
          type="secondary"
          :text="$t('core.common.buttons.save')"
          @submit="handleSubmit"
        />
        <VButton :disabled="isSubmitting" @click="modal?.close()">
          {{ $t("core.common.buttons.cancel") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
