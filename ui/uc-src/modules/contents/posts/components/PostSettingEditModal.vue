<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import PostSettingForm from "./PostSettingForm.vue";
import type { PostFormState } from "../types";
import type { Post } from "@halo-dev/api-client";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { toDatetimeLocal } from "@/utils/date";
import { usePostUpdateMutate } from "@uc/modules/contents/posts/composables/use-post-update-mutate";

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
        <VButton type="default" @click="modal?.close()">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
