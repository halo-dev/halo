<script lang="ts" setup>
import {
  VPageHeader,
  IconPages,
  IconSettings,
  IconSendPlaneFill,
  VSpace,
  VButton,
  IconSave,
  Toast,
  Dialog,
  IconEye,
} from "@halo-dev/components";
import SinglePageSettingModal from "./components/SinglePageSettingModal.vue";
import type { SinglePage, SinglePageRequest } from "@halo-dev/api-client";
import {
  computed,
  nextTick,
  onMounted,
  provide,
  ref,
  toRef,
  type ComputedRef,
} from "vue";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import cloneDeep from "lodash.clonedeep";
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
import UrlPreviewModal from "@/components/preview/UrlPreviewModal.vue";
import { contentAnnotations } from "@/constants/annotations";
import { usePageUpdateMutate } from "./composables/use-page-update-mutate";

const router = useRouter();
const { t } = useI18n();
const { mutateAsync: singlePageUpdateMutate } = usePageUpdateMutate();

// Editor providers
const { editorProviders } = useEditorExtensionPoints();
const currentEditorProvider = ref<EditorProvider>();
const storedEditorProviderName = useLocalStorage("editor-provider-name", "");

const handleChangeEditorProvider = async (provider: EditorProvider) => {
  currentEditorProvider.value = provider;
  storedEditorProviderName.value = provider.name;
  formState.value.page.metadata.annotations = {
    ...formState.value.page.metadata.annotations,
    [contentAnnotations.PREFERRED_EDITOR]: provider.name,
  };
  formState.value.content.rawType = provider.rawType;

  if (isUpdateMode.value) {
    const { data } = await singlePageUpdateMutate(formState.value.page);
    formState.value.page = data;
  }
};

// SinglePage form
const initialFormState: SinglePageRequest = {
  page: {
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
      htmlMetas: [],
    },
    apiVersion: "content.halo.run/v1alpha1",
    kind: "SinglePage",
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

const formState = ref<SinglePageRequest>(cloneDeep(initialFormState));
const saving = ref(false);
const publishing = ref(false);
const settingModal = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.page.metadata.creationTimestamp;
});

// provide some data to editor
provide<ComputedRef<string | undefined>>(
  "owner",
  computed(() => formState.value.page.spec.owner)
);
provide<ComputedRef<string | undefined>>(
  "publishTime",
  computed(() => formState.value.page.spec.publishTime)
);
provide<ComputedRef<string | undefined>>(
  "permalink",
  computed(() => formState.value.page.status?.permalink)
);

const routeQueryName = useRouteQuery<string>("name");

const handleSave = async (options?: { mute?: boolean }) => {
  try {
    if (!options?.mute) {
      saving.value = true;
    }

    //Set default title and slug
    if (!formState.value.page.spec.title) {
      formState.value.page.spec.title = t("core.page_editor.untitled");
    }
    if (!formState.value.page.spec.slug) {
      formState.value.page.spec.slug = new Date().getTime().toString();
    }

    if (isUpdateMode.value) {
      const { data } = await apiClient.singlePage.updateSinglePageContent({
        name: formState.value.page.metadata.name,
        content: formState.value.content,
      });

      formState.value.page = data;
    } else {
      const { data } = await apiClient.singlePage.draftSinglePage({
        singlePageRequest: formState.value,
      });
      formState.value.page = data;
      routeQueryName.value = data.metadata.name;

      // Clear new page content cache
      handleClearCache();
    }

    if (!options?.mute) {
      Toast.success(t("core.common.toast.save_success"));
    }

    handleClearCache(routeQueryName.value as string);
    await handleFetchContent();
  } catch (error) {
    console.error("Failed to save single page", error);
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
      const { name: singlePageName } = formState.value.page.metadata;
      const { permalink } = formState.value.page.status || {};

      await apiClient.singlePage.updateSinglePageContent({
        name: singlePageName,
        content: formState.value.content,
      });

      await apiClient.singlePage.publishSinglePage({
        name: singlePageName,
      });

      if (returnToView.value && permalink) {
        window.location.href = permalink;
      } else {
        router.back();
      }
    } else {
      formState.value.page.spec.publish = true;
      await apiClient.singlePage.draftSinglePage({
        singlePageRequest: formState.value,
      });

      // Clear new page content cache
      handleClearCache();

      router.push({ name: "SinglePages" });
    }

    Toast.success(t("core.common.toast.publish_success"));
    handleClearCache(routeQueryName.value as string);
  } catch (error) {
    console.error("Failed to publish single page", error);
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
  if (!formState.value.page.spec.headSnapshot) {
    return;
  }
  const { data } = await apiClient.singlePage.fetchSinglePageHeadContent({
    name: formState.value.page.metadata.name,
  });

  formState.value.content = Object.assign(formState.value.content, data);

  // get editor provider
  if (!currentEditorProvider.value) {
    const preferredEditor = editorProviders.value.find(
      (provider) =>
        provider.name ===
        formState.value.page.metadata.annotations?.[
          contentAnnotations.PREFERRED_EDITOR
        ]
    );
    const provider =
      preferredEditor ||
      editorProviders.value.find(
        (provider) => provider.rawType === data.rawType
      );
    if (provider) {
      currentEditorProvider.value = provider;
      formState.value.page.metadata.annotations = {
        ...formState.value.page.metadata.annotations,
        [contentAnnotations.PREFERRED_EDITOR]: provider.name,
      };

      const { data } = await singlePageUpdateMutate(formState.value.page);

      formState.value.page = data;
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

// SinglePage settings
const handleOpenSettingModal = async () => {
  const { data: latestSinglePage } =
    await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
      name: formState.value.page.metadata.name,
    });
  formState.value.page = latestSinglePage;
  settingModal.value = true;
};

const onSettingSaved = (page: SinglePage) => {
  // Set route query parameter
  if (!isUpdateMode.value) {
    routeQueryName.value = page.metadata.name;
  }

  formState.value.page = page;
  settingModal.value = false;

  if (!isUpdateMode.value) {
    handleSave();
  }
};

const onSettingPublished = (singlePage: SinglePage) => {
  formState.value.page = singlePage;
  settingModal.value = false;
  handlePublish();
};

onMounted(async () => {
  if (routeQueryName.value) {
    const { data: singlePage } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: routeQueryName.value,
      });
    formState.value.page = singlePage;

    // fetch single page content
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
    formState.value.page.metadata.annotations = {
      [contentAnnotations.PREFERRED_EDITOR]: provider.name,
    };
  }

  handleResetCache();
});

// SinglePage content cache
const { handleSetContentCache, handleResetCache, handleClearCache } =
  useContentCache(
    "singlePage-content-cache",
    routeQueryName,
    toRef(formState.value.content, "raw")
  );

// SinglePage preview
const previewModal = ref(false);
const previewPending = ref(false);

const handlePreview = async () => {
  previewPending.value = true;
  await handleSave({ mute: true });
  previewModal.value = true;
  previewPending.value = false;
};
</script>

<template>
  <SinglePageSettingModal
    v-model:visible="settingModal"
    :single-page="formState.page"
    :publish-support="!isUpdateMode"
    :only-emit="!isUpdateMode"
    @saved="onSettingSaved"
    @published="onSettingPublished"
  />

  <UrlPreviewModal
    v-if="isUpdateMode"
    v-model:visible="previewModal"
    :title="formState.page.spec.title"
    :url="`/preview/singlepages/${formState.page.metadata.name}`"
  />

  <VPageHeader :title="$t('core.page.title')">
    <template #icon>
      <IconPages class="mr-2 self-center" />
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
      class="h-full"
      @update="handleSetContentCache"
    />
  </div>
</template>
