<script lang="ts" setup>
import {
  VPageHeader,
  IconPages,
  VSpace,
  VButton,
  IconSave,
} from "@halo-dev/components";
import DefaultEditor from "@/components/editor/DefaultEditor.vue";
import SinglePageSettingModal from "./components/SinglePageSettingModal.vue";
import PostPreviewModal from "../posts/components/PostPreviewModal.vue";
import type { SinglePageRequest } from "@halo-dev/api-client";
import { v4 as uuid } from "uuid";
import { computed, onMounted, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import cloneDeep from "lodash.clonedeep";

const initialFormState: SinglePageRequest = {
  page: {
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
      htmlMetas: [],
    },
    apiVersion: "content.halo.run/v1alpha1",
    kind: "SinglePage",
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

const formState = ref<SinglePageRequest>(cloneDeep(initialFormState));
const saving = ref(false);
const settingModal = ref(false);
const previewModal = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.page.metadata.creationTimestamp;
});

const routeQueryName = useRouteQuery<string>("name");

const handleSave = async () => {
  try {
    saving.value = true;

    // Set rendered content
    formState.value.content.content = formState.value.content.raw;

    //Set default title and slug
    if (!formState.value.page.spec.title) {
      formState.value.page.spec.title = "无标题页面";
    }
    if (!formState.value.page.spec.slug) {
      formState.value.page.spec.slug = uuid();
    }

    if (isUpdateMode.value) {
      const { data } = await apiClient.singlePage.updateDraftSinglePage({
        name: formState.value.page.metadata.name,
        singlePageRequest: formState.value,
      });
      formState.value.page = data;
    } else {
      const { data } = await apiClient.singlePage.draftSinglePage({
        singlePageRequest: formState.value,
      });
      formState.value.page = data;
      routeQueryName.value = data.metadata.name;
    }
    await handleFetchContent();
  } catch (error) {
    console.error("Failed to save single page", error);
  } finally {
    saving.value = false;
  }
};

const handleFetchContent = async () => {
  if (!formState.value.page.spec.headSnapshot) {
    return;
  }
  const { data } = await apiClient.content.obtainSnapshotContent({
    snapshotName: formState.value.page.spec.headSnapshot,
  });

  formState.value.content = data;
};

const onSettingSaved = (page: SinglePageRequest) => {
  // Set route query parameter
  if (!isUpdateMode.value) {
    routeQueryName.value = page.page.metadata.name;
  }

  formState.value = page;
  settingModal.value = false;
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
  }
});
</script>

<template>
  <SinglePageSettingModal
    v-model:visible="settingModal"
    :single-page="formState"
    @saved="onSettingSaved"
  />
  <PostPreviewModal v-model:visible="previewModal" />
  <VPageHeader title="自定义页面">
    <template #icon>
      <IconPages class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <!-- TODO: add preview single page support -->
        <VButton
          v-if="false"
          size="sm"
          type="default"
          @click="previewModal = true"
        >
          预览
        </VButton>
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
  <div class="editor border-t" style="height: calc(100vh - 3.5rem)">
    <DefaultEditor
      v-model="formState.content.raw"
      :owner="formState.page.spec.owner"
      :permalink="formState.page.status?.permalink"
      :publish-time="formState.page.spec.publishTime"
    />
  </div>
</template>
