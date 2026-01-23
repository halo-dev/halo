<script lang="ts" setup>
import type AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import { contentAnnotations } from "@/constants/annotations";
import type { Content, Post } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useMutation } from "@tanstack/vue-query";
import { nextTick, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import { usePostPublishMutate } from "../composables/use-post-publish-mutate";
import type { PostFormState } from "../types";
import PostSettingForm from "./PostSettingForm.vue";

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

const { mutateAsync: postPublishMutate } = usePostPublishMutate();

const { mutate, isLoading } = useMutation({
  mutationKey: ["uc:create-post"],
  mutationFn: async ({
    data,
    annotations,
  }: {
    data: PostFormState;
    annotations: { [key: string]: string };
  }) => {
    const post: Post = {
      apiVersion: "content.halo.run/v1alpha1",
      kind: "Post",
      metadata: {
        annotations: {
          ...annotations,
          [contentAnnotations.CONTENT_JSON]: JSON.stringify(props.content),
        },
        name: utils.id.uuid(),
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
        publish: props.publish,
        publishTime: data.publishTime,
        slug: data.slug,
        tags: data.tags,
        title: data.title,
        visible: data.visible,
      },
    };

    const { data: createdPost } = await ucApiClient.content.post.createMyPost({
      post,
    });

    if (props.publish) {
      await postPublishMutate({ name: post.metadata.name });
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

const annotationsFormRef =
  useTemplateRef<InstanceType<typeof AnnotationsForm>>("annotationsFormRef");

async function onSubmit(data: PostFormState) {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();

  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } =
    annotationsFormRef.value || {};

  if (customFormInvalid || specFormInvalid) {
    return;
  }

  mutate({
    data,
    annotations: {
      ...annotations,
      ...customAnnotations,
    },
  });
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

    <div class="py-5">
      <div class="border-t border-gray-200"></div>
    </div>

    <div class="md:grid md:grid-cols-4 md:gap-6">
      <div class="md:col-span-1">
        <div class="sticky top-0">
          <span class="text-base font-medium text-gray-900">
            {{ $t("core.post.settings.groups.annotations") }}
          </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          :key="post.metadata.name"
          ref="annotationsFormRef"
          :value="post.metadata.annotations || {}"
          kind="Post"
          :form-data="post"
          group="content.halo.run"
        />
      </div>
    </div>

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
