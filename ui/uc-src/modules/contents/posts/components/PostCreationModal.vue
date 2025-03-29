<script lang="ts" setup>
import { contentAnnotations } from "@/constants/annotations";
import type { Content, Post } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { ref } from "vue";
import type { PostFormState } from "../types";
import PostSettingForm from "./PostSettingForm.vue";

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

function onSubmit(data: PostFormState) {
  const post: Post = {
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Post",
    metadata: {
      annotations: {
        [contentAnnotations.CONTENT_JSON]: JSON.stringify(props.content),
      },
      name: props.post.metadata.name,
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
  emit("success", post);
  modal.value?.close();
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
        <VButton type="secondary" @click="$formkit.submit('post-setting-form')">
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
