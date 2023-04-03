<script lang="ts" setup>
import {
  IconBookRead,
  IconSave,
  IconSettings,
  IconSendPlaneFill,
  VButton,
  VPageHeader,
  VSpace,
  Toast,
  Dialog,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import type { Post, PostRequest } from "@halo-dev/api-client";
import {
  computed,
  nextTick,
  onMounted,
  provide,
  ref,
  toRef,
  type ComputedRef,
} from "vue";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { useRouter } from "vue-router";
import { randomUUID } from "@/utils/id";
import { useContentCache } from "@/composables/use-content-cache";
import {
  useEditorExtensionPoints,
  type EditorProvider,
} from "@/composables/use-editor-extension-points";
import { useLocalStorage } from "@vueuse/core";
import EditorProviderSelector from "@/components/dropdown-selector/EditorProviderSelector.vue";
import { useI18n } from "vue-i18n";

const router = useRouter();
const { t } = useI18n();

// Editor providers
const { editorProviders } = useEditorExtensionPoints();
const currentEditorProvider = ref<EditorProvider>();
const storedEditorProviderName = useLocalStorage("editor-provider-name", "");

const handleChangeEditorProvider = (provider: EditorProvider) => {
  currentEditorProvider.value = provider;
  storedEditorProviderName.value = provider.name;
  formState.value.post.metadata.annotations = {
    "content.halo.run/preferred-editor": provider.name,
  };
  formState.value.content.rawType = provider.rawType;
};

// Post form
const initialFormState: PostRequest = {
  post: {
    spec: {
      title: "",
      slug: "",
      template: "",
      cover: "",
      deleted: false,
      publish: false,
      publishTime: undefined,
      pinned: false,
      allowComment: true,
      visible: "PUBLIC",
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
      name: randomUUID(),
      annotations: {},
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
const publishing = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.post.metadata.creationTimestamp;
});

// provide some data to editor
provide<ComputedRef<string | undefined>>(
  "owner",
  computed(() => formState.value.post.spec.owner)
);
provide<ComputedRef<string | undefined>>(
  "publishTime",
  computed(() => formState.value.post.spec.publishTime)
);
provide<ComputedRef<string | undefined>>(
  "permalink",
  computed(() => formState.value.post.status?.permalink)
);

const handleSave = async () => {
  try {
    saving.value = true;

    // Set default title and slug
    if (!formState.value.post.spec.title) {
      formState.value.post.spec.title = t("core.post_editor.untitled");
    }

    if (!formState.value.post.spec.slug) {
      formState.value.post.spec.slug = new Date().getTime().toString();
    }

    if (isUpdateMode.value) {
      const { data } = await apiClient.post.updatePostContent({
        name: formState.value.post.metadata.name,
        content: formState.value.content,
      });

      formState.value.post = data;
    } else {
      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });
      formState.value.post = data;
      name.value = data.metadata.name;
    }

    Toast.success(t("core.common.toast.save_success"));
    handleClearCache(name.value as string);
    await handleFetchContent();
  } catch (e) {
    console.error("Failed to save post", e);
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  } finally {
    saving.value = false;
  }
};

const returnToView = useRouteQuery<string>("returnToView");

const handlePublish = async () => {
  try {
    publishing.value = true;

    if (isUpdateMode.value) {
      const { name: postName } = formState.value.post.metadata;
      const { permalink } = formState.value.post.status || {};

      await apiClient.post.updatePostContent({
        name: postName,
        content: formState.value.content,
      });

      await apiClient.post.publishPost({
        name: postName,
      });

      if (returnToView.value === "true" && permalink) {
        window.location.href = permalink;
      } else {
        router.push({ name: "Posts" });
      }
    } else {
      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });

      await apiClient.post.publishPost({
        name: data.metadata.name,
      });

      router.push({ name: "Posts" });
    }

    Toast.success(t("core.common.toast.publish_success"), {
      duration: 2000,
    });
    handleClearCache(name.value as string);
  } catch (error) {
    console.error("Failed to publish post", error);
    Toast.error(t("core.common.toast.publish_failed_and_retry"));
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

  const { data } = await apiClient.post.fetchPostHeadContent({
    name: formState.value.post.metadata.name,
  });

  formState.value.content = Object.assign(formState.value.content, data);

  // get editor provider
  if (!currentEditorProvider.value) {
    const preferredEditor = editorProviders.value.find(
      (provider) =>
        provider.name ===
        formState.value.post.metadata.annotations?.[
          "content.halo.run/preferred-editor"
        ]
    );

    const provider =
      preferredEditor ||
      editorProviders.value.find(
        (provider) => provider.rawType === data.rawType
      );

    if (provider) {
      currentEditorProvider.value = provider;

      formState.value.post.metadata.annotations = {
        ...formState.value.post.metadata.annotations,
        "content.halo.run/preferred-editor": provider.name,
      };

      const { data } =
        await apiClient.extension.post.updatecontentHaloRunV1alpha1Post({
          name: formState.value.post.metadata.name,
          post: formState.value.post,
        });

      formState.value.post = data;
    } else {
      Dialog.warning({
        title: t("core.common.dialog.titles.warning"),
        description: t("core.common.dialog.descriptions.editor_not_found", {
          raw_type: data.rawType,
        }),
        confirmText: t("core.common.buttons.confirm"),
        cancelText: t("core.common.buttons.cancel"),
        onConfirm: () => {
          router.back();
        },
      });
    }

    await nextTick();
  }
};

const handleOpenSettingModal = async () => {
  const { data: latestPost } =
    await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
      name: formState.value.post.metadata.name,
    });
  formState.value.post = latestPost;
  settingModal.value = true;
};

// Post settings
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
const name = useRouteQuery<string>("name");
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
  } else {
    // Set default editor
    const provider =
      editorProviders.value.find(
        (provider) => provider.name === storedEditorProviderName.value
      ) || editorProviders.value[0];

    if (provider) {
      currentEditorProvider.value = provider;
      formState.value.content.rawType = provider.rawType;
    }

    formState.value.post.metadata.annotations = {
      "content.halo.run/preferred-editor": provider.name,
    };
  }
  handleResetCache();
});

// Post content cache
const { handleSetContentCache, handleResetCache, handleClearCache } =
  useContentCache(
    "post-content-cache",
    name,
    toRef(formState.value.content, "raw")
  );
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
  <VPageHeader :title="$t('core.post.title')">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <EditorProviderSelector
          v-if="editorProviders.length > 1 && !isUpdateMode"
          :provider="currentEditorProvider"
          @select="handleChangeEditorProvider"
        />
        <VButton :loading="saving" size="sm" type="default" @click="handleSave">
          <template #icon>
            <IconSave class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.save") }}
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
          {{ $t("core.common.buttons.setting") }}
        </VButton>
        <VButton
          type="secondary"
          :loading="publishing"
          @click="handlePublishClick"
        >
          <template #icon>
            <IconSendPlaneFill class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.publish") }}
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <div class="editor border-t" style="height: calc(100vh - 3.5rem)">
    <component
      :is="currentEditorProvider.component"
      v-if="currentEditorProvider"
      v-model:raw="formState.content.raw"
      v-model:content="formState.content.content"
      class="h-full"
      @update="handleSetContentCache"
    />
  </div>
</template>
