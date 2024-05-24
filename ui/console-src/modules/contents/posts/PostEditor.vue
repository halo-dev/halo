<script lang="ts" setup>
import {
  Dialog,
  IconBookRead,
  IconEye,
  IconHistoryLine,
  IconSave,
  IconSendPlaneFill,
  IconSettings,
  Toast,
  VButton,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import PostSettingModal from "./components/PostSettingModal.vue";
import type { Post, PostRequest } from "@halo-dev/api-client";
import {
  computed,
  type ComputedRef,
  nextTick,
  onMounted,
  provide,
  ref,
  toRef,
  watch,
} from "vue";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { useRouter } from "vue-router";
import { randomUUID } from "@/utils/id";
import { useContentCache } from "@/composables/use-content-cache";
import { useEditorExtensionPoints } from "@/composables/use-editor-extension-points";
import type { EditorProvider } from "@halo-dev/console-shared";
import { useLocalStorage } from "@vueuse/core";
import EditorProviderSelector from "@/components/dropdown-selector/EditorProviderSelector.vue";
import { useI18n } from "vue-i18n";
import UrlPreviewModal from "@/components/preview/UrlPreviewModal.vue";
import { usePostUpdateMutate } from "./composables/use-post-update-mutate";
import { contentAnnotations } from "@/constants/annotations";
import { useAutoSaveContent } from "@/composables/use-auto-save-content";
import { useContentSnapshot } from "@console/composables/use-content-snapshot";
import { useSaveKeybinding } from "@console/composables/use-save-keybinding";
import { useSessionKeepAlive } from "@/composables/use-session-keep-alive";
import { usePermission } from "@/utils/permission";
import type { AxiosRequestConfig } from "axios";

const router = useRouter();
const { t } = useI18n();
const { mutateAsync: postUpdateMutate } = usePostUpdateMutate();
const { currentUserHasPermission } = usePermission();

// Editor providers
const { editorProviders } = useEditorExtensionPoints();
const currentEditorProvider = ref<EditorProvider>();
const storedEditorProviderName = useLocalStorage("editor-provider-name", "");

const handleChangeEditorProvider = async (provider: EditorProvider) => {
  currentEditorProvider.value = provider;
  storedEditorProviderName.value = provider.name;

  formState.value.post.metadata.annotations = {
    ...formState.value.post.metadata.annotations,
    [contentAnnotations.PREFERRED_EDITOR]: provider.name,
  };

  formState.value.content.rawType = provider.rawType;

  if (isUpdateMode.value) {
    const { data } = await postUpdateMutate(formState.value.post);
    formState.value.post = data;
  }
};

// fixme: PostRequest type may be wrong
interface PostRequestWithContent extends PostRequest {
  content: {
    raw: string;
    content: string;
    rawType: string;
  };
}

// Post form
const formState = ref<PostRequestWithContent>({
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
});
const settingModal = ref(false);
const saving = ref(false);
const publishing = ref(false);

const isTitleChanged = ref(false);
watch(
  () => formState.value.post.spec.title,
  (newValue, oldValue) => {
    isTitleChanged.value = newValue !== oldValue;
  }
);

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

const handleSave = async (options?: { mute?: boolean }) => {
  try {
    if (!options?.mute) {
      saving.value = true;
    }

    // Set default title and slug
    if (!formState.value.post.spec.title) {
      formState.value.post.spec.title = t("core.post_editor.untitled");
    }

    if (!formState.value.post.spec.slug) {
      formState.value.post.spec.slug = new Date().getTime().toString();
    }

    if (isUpdateMode.value) {
      // Save post title
      if (isTitleChanged.value) {
        formState.value.post = (
          await postUpdateMutate(formState.value.post)
        ).data;
      }

      const { data } = await apiClient.post.updatePostContent({
        name: formState.value.post.metadata.name,
        content: formState.value.content,
      });

      formState.value.post = data;

      isTitleChanged.value = false;
    } else {
      // Clear new post content cache
      handleClearCache();

      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });
      formState.value.post = data;
      name.value = data.metadata.name;
    }

    if (!options?.mute) {
      Toast.success(t("core.common.toast.save_success"));
    }
    handleClearCache(formState.value.post.metadata.name as string);
    await handleFetchContent();
    await handleFetchSnapshot();
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

      if (isTitleChanged.value) {
        formState.value.post = (
          await postUpdateMutate(formState.value.post)
        ).data;
      }

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
        router.back();
      }
    } else {
      const { data } = await apiClient.post.draftPost({
        postRequest: formState.value,
      });

      await apiClient.post.publishPost({
        name: data.metadata.name,
      });

      // Clear new post content cache
      handleClearCache();

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
    // Set editor title to post
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
          contentAnnotations.PREFERRED_EDITOR
        ]
    );

    const provider =
      preferredEditor ||
      editorProviders.value.find(
        (provider) =>
          provider.rawType.toLowerCase() === data.rawType?.toLowerCase()
      );

    if (provider) {
      currentEditorProvider.value = provider;

      formState.value.post.metadata.annotations = {
        ...formState.value.post.metadata.annotations,
        [contentAnnotations.PREFERRED_EDITOR]: provider.name,
      };

      const { data } = await postUpdateMutate(formState.value.post);

      formState.value.post = data;
    } else {
      Dialog.warning({
        title: t("core.common.dialog.titles.warning"),
        description: t("core.common.dialog.descriptions.editor_not_found", {
          raw_type: data.rawType,
        }),
        confirmText: t("core.common.buttons.confirm"),
        showCancel: false,
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
    await apiClient.extension.post.getContentHaloRunV1alpha1Post({
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

  if (!isUpdateMode.value) {
    handleSave();
  }
};

const onSettingPublished = (post: Post) => {
  formState.value.post = post;
  handlePublish();
};

// Get post data when the route contains the name parameter
const name = useRouteQuery<string>("name");
onMounted(async () => {
  if (name.value) {
    // fetch post
    const { data: post } =
      await apiClient.extension.post.getContentHaloRunV1alpha1Post({
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
      [contentAnnotations.PREFERRED_EDITOR]: provider.name,
    };
  }
  handleResetCache();
});

const headSnapshot = computed(() => {
  return formState.value.post.spec.headSnapshot;
});

const { version, handleFetchSnapshot } = useContentSnapshot(headSnapshot);

// Post content cache
const {
  currentCache,
  handleSetContentCache,
  handleResetCache,
  handleClearCache,
} = useContentCache(
  "post-content-cache",
  name,
  toRef(formState.value.content, "raw"),
  version
);

useAutoSaveContent(currentCache, toRef(formState.value.content, "raw"), () => {
  // Do not save when the setting modal is open
  if (settingModal.value) {
    return;
  }
  handleSave({ mute: true });
});

// Post preview
const previewModal = ref(false);
const previewPending = ref(false);

const handlePreview = async () => {
  previewPending.value = true;
  await handleSave({ mute: true });
  previewModal.value = true;
  previewPending.value = false;
};

useSaveKeybinding(handleSave);

// Keep session alive
useSessionKeepAlive();

// Upload image
async function handleUploadImage(file: File, options?: AxiosRequestConfig) {
  if (!currentUserHasPermission(["uc:attachments:manage"])) {
    return;
  }

  if (!isUpdateMode.value) {
    await handleSave();
  }

  const { data } = await apiClient.uc.attachment.createAttachmentForPost(
    {
      file,
      postName: formState.value.post.metadata.name,
      waitForPermalink: true,
    },
    options
  );
  return data;
}
</script>

<template>
  <PostSettingModal
    v-if="settingModal"
    :post="formState.post"
    :publish-support="!isUpdateMode"
    :only-emit="!isUpdateMode"
    @close="settingModal = false"
    @saved="onSettingSaved"
    @published="onSettingPublished"
  />

  <UrlPreviewModal
    v-if="previewModal"
    :title="formState.post.spec.title"
    :url="`/preview/posts/${formState.post.metadata.name}`"
    @close="previewModal = false"
  />

  <VPageHeader :title="$t('core.post.title')">
    <template #icon>
      <IconBookRead class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <EditorProviderSelector
          v-if="editorProviders.length > 1"
          :provider="currentEditorProvider"
          :allow-forced-select="!isUpdateMode"
          @select="handleChangeEditorProvider"
        />
        <VButton
          v-if="isUpdateMode"
          size="sm"
          type="default"
          @click="
            $router.push({ name: 'PostSnapshots', query: { name: name } })
          "
        >
          <template #icon>
            <IconHistoryLine class="h-full w-full" />
          </template>
          {{ $t("core.post_editor.actions.snapshots") }}
        </VButton>
        <VButton
          size="sm"
          type="default"
          :loading="previewPending"
          @click="handlePreview"
        >
          <template #icon>
            <IconEye class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.preview") }}
        </VButton>
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
      v-model:title="formState.post.spec.title"
      :upload-image="handleUploadImage"
      class="h-full"
      @update="handleSetContentCache"
    />
  </div>
</template>
