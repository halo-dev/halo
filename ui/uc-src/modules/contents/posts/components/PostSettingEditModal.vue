<script lang="ts" setup>
import { toDatetimeLocal } from "@/utils/date";
import type { Post } from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { usePostUpdateMutate } from "@uc/modules/contents/posts/composables/use-post-update-mutate";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import type { PostFormState } from "../types";
import PostSettingForm from "./PostSettingForm.vue";

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

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { mutateAsync, isLoading } = usePostUpdateMutate();

async function onSubmit(data: PostFormState) {
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

  const { data: newPost } = await mutateAsync({ postToUpdate });

  Toast.success(t("core.common.toast.save_success"));
  emit("success", newPost);
  modal.value?.close();
}
</script>

<template>
  <VModal
    ref="modal"
    :title="$t('core.uc_post.setting_modal.title')"
    :width="700"
    centered
    @close="emit('close')"
  >
    <PostSettingForm
      :form-state="{
        title: post.spec.title,
        slug: post.spec.slug,
        cover: post.spec.cover,
        categories: post.spec.categories,
        tags: post.spec.tags,
        allowComment: post.spec.allowComment,
        visible: post.spec.visible,
        pinned: post.spec.pinned,
        publishTime: post.spec.publishTime
          ? toDatetimeLocal(post.spec.publishTime)
          : undefined,
        excerptAutoGenerate: post.spec.excerpt.autoGenerate,
        excerptRaw: post.spec.excerpt.raw,
      }"
      :name="post.metadata.name"
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
        <VButton type="default" @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
