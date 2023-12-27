<script lang="ts" setup>
import { useEditorExtensionPoints } from "@/composables/use-editor-extension-points";
import type { EditorProvider } from "@halo-dev/console-shared";
import {
  Dialog,
  IconBookRead,
  IconSave,
  IconSendPlaneFill,
  IconSettings,
  Toast,
  VButton,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import EditorProviderSelector from "@/components/dropdown-selector/EditorProviderSelector.vue";
import { ref } from "vue";
import { useLocalStorage } from "@vueuse/core";
import type { Post, Content, Snapshot } from "@halo-dev/api-client";
import { randomUUID } from "@/utils/id";
import { contentAnnotations } from "@/constants/annotations";
import { useRouteQuery } from "@vueuse/router";
import { onMounted } from "vue";
import { apiClient } from "@/utils/api-client";
import { nextTick } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useMutation } from "@tanstack/vue-query";
import { computed } from "vue";
import { useSaveKeybinding } from "@console/composables/use-save-keybinding";
import PostCreationModal from "./components/PostCreationModal.vue";
import PostSettingEditModal from "./components/PostSettingEditModal.vue";
import HasPermission from "@/components/permission/HasPermission.vue";
import { provide } from "vue";
import type { ComputedRef } from "vue";
import { useSessionKeepAlive } from "@/composables/use-session-keep-alive";
import { usePermission } from "@/utils/permission";

const router = useRouter();
const { t } = useI18n();
const { currentUserHasPermission } = usePermission();

const formState = ref<Post>({
  apiVersion: "content.halo.run/v1alpha1",
  kind: "Post",
  metadata: {
    annotations: {},
    name: randomUUID(),
  },
  spec: {
    allowComment: true,
    baseSnapshot: "",
    categories: [],
    cover: "",
    deleted: false,
    excerpt: {
      autoGenerate: true,
      raw: "",
    },
    headSnapshot: "",
    htmlMetas: [],
    owner: "",
    pinned: false,
    priority: 0,
    publish: false,
    publishTime: "",
    releaseSnapshot: "",
    slug: "",
    tags: [],
    template: "",
    title: "",
    visible: "PUBLIC",
  },
});

const content = ref<Content>({
  content: "",
  raw: "",
  rawType: "",
});
const snapshot = ref<Snapshot>();

// provide some data to editor
provide<ComputedRef<string | undefined>>(
  "owner",
  computed(() => formState.value.spec.owner)
);
provide<ComputedRef<string | undefined>>(
  "publishTime",
  computed(() => formState.value.spec.publishTime)
);
provide<ComputedRef<string | undefined>>(
  "permalink",
  computed(() => formState.value.status?.permalink)
);

// Editor providers
const { editorProviders } = useEditorExtensionPoints();
const currentEditorProvider = ref<EditorProvider>();
const storedEditorProviderName = useLocalStorage("editor-provider-name", "");

const handleChangeEditorProvider = async (provider: EditorProvider) => {
  currentEditorProvider.value = provider;

  const { name, rawType } = provider;

  storedEditorProviderName.value = name;

  content.value.rawType = rawType;

  formState.value.metadata.annotations = {
    ...formState.value.metadata.annotations,
    [contentAnnotations.PREFERRED_EDITOR]: name,
  };
};

// Fetch post data when the route contains the name parameter
const name = useRouteQuery<string | undefined>("name");

onMounted(async () => {
  if (name.value) {
    const { data: post } = await apiClient.uc.post.getMyPost({
      name: name.value,
    });

    formState.value = post;

    await handleFetchContent();
    return;
  }

  // New post, set default editor
  const provider =
    editorProviders.value.find(
      (provider) => provider.name === storedEditorProviderName.value
    ) || editorProviders.value[0];

  if (provider) {
    currentEditorProvider.value = provider;
    content.value.rawType = provider.rawType;
    formState.value.metadata.annotations = {
      [contentAnnotations.PREFERRED_EDITOR]: provider.name,
    };
  }
});

/**
 * Fetch content from the head snapshot.
 */
async function handleFetchContent() {
  const { headSnapshot } = formState.value.spec || {};

  if (!headSnapshot || !name.value) {
    return;
  }

  const { data } = await apiClient.uc.post.getMyPostDraft({
    name: name.value,
    patched: true,
  });

  const {
    [contentAnnotations.PATCHED_CONTENT]: patchedContent,
    [contentAnnotations.PATCHED_RAW]: patchedRaw,
  } = data.metadata.annotations || {};

  const { rawType } = data.spec || {};

  content.value = {
    content: patchedContent,
    raw: patchedRaw,
    rawType,
  };

  snapshot.value = data;

  if (currentEditorProvider.value) {
    return;
  }

  await handleSetEditorProviderFromRemote();
}

async function handleSetEditorProviderFromRemote() {
  const { [contentAnnotations.PREFERRED_EDITOR]: preferredEditorName } =
    formState.value.metadata.annotations || {};

  const preferredEditor = editorProviders.value.find(
    (provider) => provider.name === preferredEditorName
  );

  const provider =
    preferredEditor ||
    editorProviders.value.find(
      (provider) => provider.rawType === content.value.rawType
    );

  if (provider) {
    currentEditorProvider.value = provider;

    formState.value.metadata.annotations = {
      ...formState.value.metadata.annotations,
      [contentAnnotations.PREFERRED_EDITOR]: provider.name,
    };
  } else {
    Dialog.warning({
      title: t("core.common.dialog.titles.warning"),
      description: t("core.common.dialog.descriptions.editor_not_found", {
        raw_type: content.value.rawType,
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

// Create post
const postCreationModal = ref(false);

function handleSaveClick() {
  if (isUpdateMode.value) {
    handleSave({ mute: false });
  } else {
    postCreationModal.value = true;
  }
}

async function onCreatePostSuccess(data: Post) {
  formState.value = data;
  // Update route query params
  name.value = data.metadata.name;
  await handleFetchContent();
}

// Save post
const isUpdateMode = computed(
  () => !!formState.value.metadata.creationTimestamp
);

const { mutateAsync: handleSave, isLoading: isSaving } = useMutation({
  mutationKey: ["save-post"],
  variables: {
    mute: false,
  },
  mutationFn: async () => {
    // Snapshot always exists in update mode
    if (!snapshot.value) {
      return;
    }

    const { annotations } = snapshot.value.metadata || {};

    snapshot.value.metadata.annotations = {
      ...annotations,
      [contentAnnotations.CONTENT_JSON]: JSON.stringify(content.value),
    };

    if (!isUpdateMode.value || !name.value) {
      return;
    }

    const { data } = await apiClient.uc.post.updateMyPostDraft({
      name: name.value,
      snapshot: snapshot.value,
    });

    snapshot.value = data;

    return data;
  },
  onSuccess(_, variables) {
    if (!variables.mute) Toast.success(t("core.common.toast.save_success"));
    handleFetchContent();
  },
  onError() {
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  },
});

useSaveKeybinding(handleSaveClick);

// Publish post

const postPublishModal = ref(false);

function handlePublishClick() {
  if (isUpdateMode.value) {
    handlePublish();
  } else {
    postPublishModal.value = true;
  }
}

function onPublishPostSuccess() {
  router.push({ name: "Posts" });
}

const { mutateAsync: handlePublish, isLoading: isPublishing } = useMutation({
  mutationKey: ["publish-post"],
  mutationFn: async () => {
    await handleSave({ mute: true });

    return await apiClient.uc.post.publishMyPost({
      name: formState.value.metadata.name,
    });
  },
  onSuccess() {
    Toast.success(t("core.common.toast.publish_success"), {
      duration: 2000,
    });

    router.push({ name: "Posts" });
  },
  onError() {
    Toast.error(t("core.common.toast.publish_failed_and_retry"));
  },
});

// Post setting
const postSettingEditModal = ref(false);

function handleOpenPostSettingEditModal() {
  handleSave({ mute: true });
  postSettingEditModal.value = true;
}

function onUpdatePostSuccess(data: Post) {
  formState.value = data;
  handleFetchContent();
}

// Upload image
async function handleUploadImage(file: File) {
  if (!currentUserHasPermission(["uc:attachments:manage"])) {
    return;
  }
  if (!isUpdateMode.value) {
    formState.value.metadata.annotations = {
      ...formState.value.metadata.annotations,
      [contentAnnotations.CONTENT_JSON]: JSON.stringify(content.value),
    };

    if (!formState.value.spec.title) {
      formState.value.spec.title = t("core.post_editor.untitled");
    }

    if (!formState.value.spec.slug) {
      formState.value.spec.slug = new Date().getTime().toString();
    }

    const { data } = await apiClient.uc.post.createMyPost({
      post: formState.value,
    });

    await onCreatePostSuccess(data);
  }

  const { data } = await apiClient.uc.attachment.createAttachmentForPost({
    file,
    postName: formState.value.metadata.name,
    waitForPermalink: true,
  });
  return data;
}

// Keep session alive
useSessionKeepAlive();
</script>

<template>
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
          size="sm"
          type="default"
          :loading="isSaving && !isPublishing"
          @click="handleSaveClick"
        >
          <template #icon>
            <IconSave class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton
          v-if="isUpdateMode"
          size="sm"
          type="default"
          @click="handleOpenPostSettingEditModal"
        >
          <template #icon>
            <IconSettings class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.setting") }}
        </VButton>
        <HasPermission :permissions="['uc:posts:publish']">
          <VButton
            :loading="isPublishing"
            type="secondary"
            @click="handlePublishClick"
          >
            <template #icon>
              <IconSendPlaneFill class="h-full w-full" />
            </template>
            {{ $t("core.common.buttons.publish") }}
          </VButton>
        </HasPermission>
      </VSpace>
    </template>
  </VPageHeader>
  <div class="editor border-t" style="height: calc(100vh - 3.5rem)">
    <component
      :is="currentEditorProvider.component"
      v-if="currentEditorProvider"
      v-model:raw="content.raw"
      v-model:content="content.content"
      :upload-image="handleUploadImage"
      class="h-full"
    />
  </div>

  <PostCreationModal
    v-if="postCreationModal"
    :title="$t('core.uc_post.creation_modal.title')"
    :content="content"
    @close="postCreationModal = false"
    @success="onCreatePostSuccess"
  />

  <PostCreationModal
    v-if="postPublishModal"
    :title="$t('core.uc_post.publish_modal.title')"
    :content="content"
    publish
    @close="postPublishModal = false"
    @success="onPublishPostSuccess"
  />

  <PostSettingEditModal
    v-if="postSettingEditModal"
    :post="formState"
    @close="postSettingEditModal = false"
    @success="onUpdatePostSuccess"
  />
</template>
