<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { ref } from "vue";
import PostSettingForm from "./PostSettingForm.vue";
import type { Content, Post } from "@halo-dev/api-client";
import type { PostFormState } from "../types";
import { useMutation } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import { randomUUID } from "@/utils/id";
import { contentAnnotations } from "@/constants/annotations";
import { apiClient } from "@/utils/api-client";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    title: string;
    content: Content;
    publish?: boolean;
    post: Post;
  }>(),
  {
    publish: false,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "success", post: Post): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { mutate, isLoading } = useMutation({
  mutationKey: ["uc:create-post"],
  mutationFn: async ({ data }: { data: PostFormState }) => {
    const post: Post = {
      apiVersion: "content.halo.run/v1alpha1",
      kind: "Post",
      metadata: {
        annotations: {
          [contentAnnotations.CONTENT_JSON]: JSON.stringify(props.content),
        },
        name: randomUUID(),
      },
      spec: {
        allowComment: data.allowComment,
        categories: data.categories,
        cover: data.cover,
        deleted: false,
        excerpt: {
          autoGenerate: data.excerptAutoGenerate,
          raw: data.excerptRaw,
        },
        htmlMetas: [],
        pinned: data.pinned,
        priority: 0,
        publish: false,
        publishTime: data.publishTime,
        slug: data.slug,
        tags: data.tags,
        title: data.title,
        visible: data.visible,
      },
    };

    const { data: createdPost } = await apiClient.uc.post.createMyPost({
      post,
    });

    if (props.publish) {
      await apiClient.uc.post.publishMyPost({
        name: post.metadata.name,
      });
    }

    return createdPost;
  },
  onSuccess(data) {
    if (props.publish) {
      Toast.success(t("core.common.toast.publish_success"));
    } else {
      Toast.success(t("core.common.toast.save_success"));
    }

    emit("success", data);
    modal.value?.close();
  },
  onError() {
    if (props.publish) {
      Toast.error(t("core.common.toast.publish_failed_and_retry"));
    } else {
      Toast.error(t("core.common.toast.save_failed_and_retry"));
    }
  },
});

function onSubmit(data: PostFormState) {
  mutate({ data });
}
</script>

<template>
  <VModal
    ref="modal"
    :title="title"
    :width="700"
    centered
    @close="emit('close')"
  >
    <PostSettingForm
      :form-state="{
        title: props.post.spec.title,
        slug: props.post.spec.slug,
        allowComment: props.post.spec.allowComment,
        visible: props.post.spec.visible,
        pinned: props.post.spec.pinned,
        excerptAutoGenerate: props.post.spec.excerpt.autoGenerate,
      }"
      @submit="onSubmit"
    />

    <template #footer>
      <VSpace>
        <VButton
          :loading="isLoading"
          type="secondary"
          @click="$formkit.submit('post-setting-form')"
        >
          {{
            props.publish
              ? $t("core.common.buttons.publish")
              : $t("core.common.buttons.save")
          }}
        </VButton>
        <VButton type="default" @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
