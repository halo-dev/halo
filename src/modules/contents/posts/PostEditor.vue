<script lang="ts" setup>
import {
  IconBookRead,
  IconSave,
  IconSettings,
  IconSendPlaneFill,
  VButton,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import DefaultEditor from "@/components/editor/DefaultEditor.vue";
import PostSettingModal from "./components/PostSettingModal.vue";
import PostPreviewModal from "./components/PostPreviewModal.vue";
import type { Post, PostRequest } from "@halo-dev/api-client";
import { computed, onMounted, ref } from "vue";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { v4 as uuid } from "uuid";
import { useRouter } from "vue-router";

const router = useRouter();

const initialFormState: PostRequest = {
  post: {
    spec: {
      title: "",
      slug: "",
      template: "",
      cover: "",
      deleted: false,
      publish: false,
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

const formState = ref<PostRequest>(cloneDeep(initialFormState));
const settingModal = ref(false);
const previewModal = ref(false);
const saving = ref(false);
const publishing = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.post.metadata.creationTimestamp;
});

const handleSave = async () => {
  try {
    saving.value = true;

    // Set rendered content
    formState.value.content.content = formState.value.content.raw;

    // Set default title and slug
    if (!formState.value.post.spec.title) {
      formState.value.post.spec.title = "无标题文章";
    }

    if (!formState.value.post.spec.slug) {
      formState.value.post.spec.slug = uuid();
    }

    if (isUpdateMode.value) {
      // Get latest post
      const { data: latestPost } =
        await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
          name: formState.value.post.metadata.name,
        });

      formState.value.post = latestPost;

      const { data } = await apiClient.post.updateDraftPost({
        name: formState.value.post.metadata.name,
        postRequest: formState.value,
      });

      formState.value.post = data;
    } else {
      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });
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

const handlePublish = async () => {
  try {
    publishing.value = true;

    // Set rendered content
    formState.value.content.content = formState.value.content.raw;

    if (isUpdateMode.value) {
      const { headSnapshot } = formState.value.post.spec;
      const { name: postName } = formState.value.post.metadata;
      const { data: latestContent } =
        await apiClient.content.updateSnapshotContent({
          snapshotName: headSnapshot as string,
          contentRequest: {
            raw: formState.value.content.raw as string,
            content: formState.value.content.content as string,
            rawType: formState.value.content.rawType as string,
            headSnapshotName: headSnapshot,
            subjectRef: {
              kind: "Post",
              version: "v1alpha1",
              group: "content.halo.run",
              name: postName,
            },
          },
        });

      await apiClient.post.publishPost({
        name: postName,
        headSnapshot: latestContent.snapshotName,
      });
    } else {
      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });

      await apiClient.post.publishPost({
        name: data.metadata.name,
      });
    }

    router.push({ name: "Posts" });
  } catch (error) {
    console.error("Failed to publish post", error);
  } finally {
    publishing.value = false;
  }
};

const handlePublishClick = () => {
  if (isUpdateMode.value) {
    handlePublish();
  } else {
    settingModal.value = true;
  }
};

const handleFetchContent = async () => {
  if (!formState.value.post.spec.headSnapshot) {
    return;
  }

  const { data } = await apiClient.content.obtainSnapshotContent({
    snapshotName: formState.value.post.spec.headSnapshot,
  });

  formState.value.content = data;
};

const handleOpenSettingModal = async () => {
  const { data: latestPost } =
    await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
      name: formState.value.post.metadata.name,
    });
  formState.value.post = latestPost;
  settingModal.value = true;
};

const onSettingSaved = (post: Post) => {
  // Set route query parameter
  if (!isUpdateMode.value) {
    name.value = post.metadata.name;
  }

  formState.value.post = post;
  settingModal.value = false;

  if (!isUpdateMode.value) {
    handleSave();
  }
};

const onSettingPublished = (post: Post) => {
  formState.value.post = post;
  settingModal.value = false;
  handlePublish();
};

// Get post data when the route contains the name parameter
const name = useRouteQuery("name");
onMounted(async () => {
  if (name.value) {
    // fetch post
    const { data: post } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: name.value as string,
      });
    formState.value.post = post;

    // fetch post content
    await handleFetchContent();
  }
});
</script>

<template>
  <PostSettingModal
    v-model:visible="settingModal"
    :post="formState.post"
    :publish-support="!isUpdateMode"
    :only-emit="!isUpdateMode"
    @saved="onSettingSaved"
    @published="onSettingPublished"
  />
  <PostPreviewModal v-model:visible="previewModal" :post="formState.post" />
  <VPageHeader title="文章">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <!-- TODO: add preview post support -->
        <VButton
          v-if="false"
          size="sm"
          type="default"
          @click="previewModal = true"
        >
          预览
        </VButton>
        <VButton :loading="saving" size="sm" type="default" @click="handleSave">
          <template #icon>
            <IconSave class="h-full w-full" />
          </template>
          保存
        </VButton>
        <VButton
          v-if="isUpdateMode"
          size="sm"
          type="default"
          @click="handleOpenSettingModal"
        >
          <template #icon>
            <IconSettings class="h-full w-full" />
          </template>
          设置
        </VButton>
        <VButton
          type="secondary"
          :loading="publishing"
          @click="handlePublishClick"
        >
          <template #icon>
            <IconSendPlaneFill class="h-full w-full" />
          </template>
          发布
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <div class="editor border-t" style="height: calc(100vh - 3.5rem)">
    <DefaultEditor
      v-model="formState.content.raw"
      :owner="formState.post.spec.owner"
      :permalink="formState.post.status?.permalink"
      :publish-time="formState.post.spec.publishTime"
    />
  </div>
</template>
