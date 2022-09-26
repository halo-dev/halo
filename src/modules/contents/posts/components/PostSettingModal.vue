<script lang="ts" setup>
import { VButton, VModal, VSpace, VTabItem, VTabs } from "@halo-dev/components";
import { computed, ref, watch, watchEffect } from "vue";
import type { PostRequest } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { usePostTag } from "@/modules/contents/posts/tags/composables/use-post-tag";
import { usePostCategory } from "@/modules/contents/posts/categories/composables/use-post-category";
import { apiClient } from "@/utils/api-client";
import { v4 as uuid } from "uuid";

const initialFormState: PostRequest = {
  post: {
    spec: {
      title: "",
      slug: "",
      template: "",
      cover: "",
      deleted: false,
      published: false,
      publishTime: "",
      pinned: false,
      allowComment: true,
      visible: "PUBLIC",
      version: 1,
      priority: 0,
      excerpt: {
        autoGenerate: true,
        raw: "",
      },
      categories: [],
      tags: [],
      htmlMetas: [],
    },
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Post",
    metadata: {
      name: uuid(),
    },
  },
  content: {
    raw: "",
    content: "",
    rawType: "HTML",
  },
};

const props = withDefaults(
  defineProps<{
    visible: boolean;
    post?: PostRequest | null;
  }>(),
  {
    visible: false,
    post: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", post: PostRequest): void;
}>();

const activeTab = ref("general");
const formState = ref<PostRequest>(cloneDeep(initialFormState));
const saving = ref(false);
const publishing = ref(false);
const publishCanceling = ref(false);

const { categories, handleFetchCategories } = usePostCategory();
const categoriesMap = computed(() => {
  return categories.value.map((category) => {
    return {
      value: category.metadata.name,
      label: category.spec.displayName,
    };
  });
});

const { tags, handleFetchTags } = usePostTag();
const tagsMap = computed(() => {
  return tags.value.map((tag) => {
    return {
      value: tag.metadata.name,
      label: tag.spec.displayName,
    };
  });
});

const isUpdateMode = computed(() => {
  return !!formState.value.post.metadata.creationTimestamp;
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleSave = async () => {
  try {
    saving.value = true;

    // Set rendered content
    formState.value.content.content = formState.value.content.raw;

    if (isUpdateMode.value) {
      const { data } = await apiClient.post.updateDraftPost({
        name: formState.value.post.metadata.name,
        postRequest: formState.value,
      });
      formState.value.post = data;
      emit("saved", formState.value);
    } else {
      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });
      formState.value.post = data;
      emit("saved", formState.value);
    }
  } catch (e) {
    console.error("Failed to save post", e);
  } finally {
    saving.value = false;
  }
};

const handlePublish = async () => {
  try {
    publishing.value = true;

    // Save post
    await handleSave();

    // Get latest version post
    const { data: latestData } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: formState.value.post.metadata.name,
      });
    formState.value.post = latestData;

    // Publish post
    const { data } = await apiClient.post.publishPost({
      name: formState.value.post.metadata.name,
    });
    formState.value.post = data;
    emit("saved", formState.value);
  } catch (e) {
    console.error("Failed to publish post", e);
  } finally {
    publishing.value = false;
  }
};

const handlePublishCanceling = async () => {
  try {
    publishCanceling.value = true;

    // Update published spec = false
    const postToUpdate = cloneDeep(formState.value);
    postToUpdate.post.spec.published = false;
    await apiClient.post.updateDraftPost({
      name: postToUpdate.post.metadata.name,
      postRequest: postToUpdate,
    });

    // Get latest version post
    const { data: latestData } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: formState.value.post.metadata.name,
      });

    formState.value.post = latestData;
    emit("saved", formState.value);
  } catch (e) {
    console.log("Failed to cancel publish", e);
  } finally {
    publishCanceling.value = false;
  }
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      handleFetchCategories();
      handleFetchTags();
    }
  }
);

watchEffect(() => {
  if (props.post) {
    formState.value = cloneDeep(props.post);
  }
});
</script>
<template>
  <VModal
    :visible="visible"
    :width="700"
    title="文章设置"
    @update:visible="handleVisibleChange"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>

    <VTabs v-model:active-id="activeTab" type="outline">
      <VTabItem id="general" label="常规">
        <FormKit
          id="basic"
          name="basic"
          :actions="false"
          :preserve="true"
          type="form"
        >
          <FormKit
            v-model="formState.post.spec.title"
            label="标题"
            type="text"
            name="title"
            validation="required"
          ></FormKit>
          <FormKit
            v-model="formState.post.spec.slug"
            label="别名"
            name="slug"
            type="text"
            validation="required"
          ></FormKit>
          <FormKit
            v-model="formState.post.spec.categories"
            :options="categoriesMap"
            label="分类目录"
            name="categories"
            type="checkbox"
          />
          <FormKit
            v-model="formState.post.spec.tags"
            :options="tagsMap"
            label="标签"
            name="tags"
            type="checkbox"
          />
          <FormKit
            v-model="formState.post.spec.excerpt.autoGenerate"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            name="autoGenerate"
            label="自动生成摘要"
            type="radio"
          >
          </FormKit>
          <FormKit
            v-if="!formState.post.spec.excerpt.autoGenerate"
            v-model="formState.post.spec.excerpt.raw"
            label="自定义摘要"
            name="raw"
            type="textarea"
          ></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="advanced" label="高级">
        <FormKit
          id="advanced"
          name="advanced"
          :actions="false"
          :preserve="true"
          type="form"
        >
          <FormKit
            v-model="formState.post.spec.allowComment"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="禁止评论"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.post.spec.pinned"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="是否置顶"
            name="pinned"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.post.spec.visible"
            :options="[
              { label: '公开', value: 'PUBLIC' },
              { label: '内部成员可访问', value: 'INTERNAL' },
              { label: '私有', value: 'PRIVATE' },
            ]"
            label="可见性"
            name="visible"
            type="select"
          ></FormKit>
          <FormKit
            v-model="formState.post.spec.publishTime"
            label="发表时间"
            type="datetime-local"
          ></FormKit>
          <FormKit
            v-model="formState.post.spec.template"
            label="自定义模板"
            name="template"
            type="text"
          ></FormKit>
          <FormKit
            v-model="formState.post.spec.cover"
            name="cover"
            label="封面图"
            type="text"
          ></FormKit>
        </FormKit>
      </VTabItem>
      <!--TODO: add SEO/Metas/Inject Code form-->
    </VTabs>

    <template #footer>
      <VSpace>
        <VButton :loading="publishing" type="secondary" @click="handlePublish">
          {{
            formState.post.status?.phase === "PUBLISHED" ? "重新发布" : "发布"
          }}
        </VButton>
        <VButton
          :loading="saving"
          size="sm"
          type="secondary"
          @click="handleSave"
        >
          仅保存
        </VButton>
        <VButton
          v-if="formState.post.status?.phase === 'PUBLISHED'"
          :loading="publishCanceling"
          type="danger"
          size="sm"
          @click="handlePublishCanceling"
        >
          取消发布
        </VButton>
        <VButton size="sm" type="default" @click="handleVisibleChange(false)">
          关闭
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
