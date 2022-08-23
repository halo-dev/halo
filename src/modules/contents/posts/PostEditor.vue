<script lang="ts" setup>
import {
  IconBookRead,
  IconSave,
  VButton,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import type { PostRequest } from "@halo-dev/api-client";
import { computed, onMounted, ref } from "vue";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@halo-dev/admin-shared";
import { useRouteQuery } from "@vueuse/router";
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
      publishTime: undefined,
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

const formState = ref<PostRequest>(cloneDeep(initialFormState));
const settingModal = ref(false);
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.post.metadata.creationTimestamp;
});

const handleSave = async () => {
  try {
    saving.value = true;

    // Set rendered content
    formState.value.content.content = formState.value.content.raw;

    if (isUpdateMode.value) {
      const { data } = await apiClient.post.updateDraftPost(
        formState.value.post.metadata.name,
        formState.value
      );
      formState.value.post = data;
    } else {
      const { data } = await apiClient.post.draftPost(formState.value);
      formState.value.post = data;
      name.value = data.metadata.name;
    }

    await handleFetchContent();
  } catch (e) {
    console.error("Failed to save post", e);
  } finally {
    saving.value = false;
  }
};

const handleFetchContent = async () => {
  if (!formState.value.post.spec.headSnapshot) {
    return;
  }
  const { data } = await apiClient.content.obtainSnapshotContent(
    formState.value.post.spec.headSnapshot
  );

  formState.value.content = data;
};

const onSettingSaved = (post: PostRequest) => {
  // Set route query parameter
  if (!isUpdateMode.value) {
    name.value = post.post.metadata.name;
  }

  formState.value = post;
  settingModal.value = false;
};

// Get post data when the route contains the name parameter
const name = useRouteQuery("name");
onMounted(async () => {
  if (name.value) {
    // fetch post
    const { data: post } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post(
        name.value as string
      );
    formState.value.post = post;

    // fetch post content
    await handleFetchContent();
  }
});
</script>

<template>
  <PostSettingModal
    v-model:visible="settingModal"
    :post="formState"
    @saved="onSettingSaved"
  />
  <VPageHeader title="文章">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton :loading="saving" size="sm" type="default" @click="handleSave">
          保存
        </VButton>
        <VButton type="secondary" @click="settingModal = true">
          <template #icon>
            <IconSave class="h-full w-full" />
          </template>
          发布
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <div class="editor border-t">
    <RichTextEditor v-model="formState.content.raw" />
  </div>
</template>
