<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import PostSettingForm from "./PostSettingForm.vue";
import type { PostFormState } from "../types";
import type { Post } from "@halo-dev/api-client";
import { onMounted } from "vue";
import { nextTick, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useMutation } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { toDatetimeLocal } from "@/utils/date";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    post: Post;
  }>(),
  {}
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "success", post: Post): void;
}>();

// fixme: refactor VModal component
const shouldRender = ref(false);
const visible = ref(false);
onMounted(() => {
  shouldRender.value = true;
  nextTick(() => {
    visible.value = true;
  });
});
function onClose() {
  visible.value = false;
  setTimeout(() => {
    shouldRender.value = false;
    emit("close");
  }, 200);
}

const { mutate, isLoading } = useMutation({
  mutationKey: ["edit-post"],
  mutationFn: async ({ data }: { data: PostFormState }) => {
    const postToUpdate: Post = {
      ...props.post,
      spec: {
        ...props.post.spec,
        allowComment: data.allowComment,
        categories: data.categories,
        cover: data.cover,
        excerpt: {
          autoGenerate: data.excerptAutoGenerate,
          raw: data.excerptRaw,
        },
        pinned: data.pinned,
        publishTime: data.publishTime,
        slug: data.slug,
        tags: data.tags,
        title: data.title,
        visible: data.visible,
      },
    };
    const { data: updatedPost } = await apiClient.uc.post.updateMyPost({
      name: props.post.metadata.name,
      post: postToUpdate,
    });
    return updatedPost;
  },
  onSuccess(data) {
    Toast.success(t("core.common.toast.save_success"));
    emit("success", data);
    emit("close");
  },
  onError() {
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  },
});

function onSubmit(data: PostFormState) {
  mutate({ data });
}
</script>

<template>
  <VModal
    v-if="shouldRender"
    v-model:visible="visible"
    :title="$t('core.uc_post.setting_modal.title')"
    :width="700"
    centered
    @close="onClose"
  >
    <PostSettingForm
      :form-state="{
        title: props.post.spec.title,
        slug: props.post.spec.slug,
        cover: props.post.spec.cover,
        categories: props.post.spec.categories,
        tags: props.post.spec.tags,
        allowComment: props.post.spec.allowComment,
        visible: props.post.spec.visible,
        pinned: props.post.spec.pinned,
        publishTime: props.post.spec.publishTime
          ? toDatetimeLocal(props.post.spec.publishTime)
          : undefined,
        excerptAutoGenerate: props.post.spec.excerpt.autoGenerate,
        excerptRaw: props.post.spec.excerpt.raw,
      }"
      update-mode
      @submit="onSubmit"
    />

    <template #footer>
      <VSpace>
        <VButton
          :loading="isLoading"
          type="secondary"
          @click="$formkit.submit('post-setting-form')"
        >
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton type="default" @click="onClose()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
