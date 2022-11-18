<script lang="ts" setup>
import {
  VPageHeader,
  IconPages,
  IconSettings,
  IconSendPlaneFill,
  VSpace,
  VButton,
  IconSave,
} from "@halo-dev/components";
import DefaultEditor from "@/components/editor/DefaultEditor.vue";
import SinglePageSettingModal from "./components/SinglePageSettingModal.vue";
import PostPreviewModal from "../posts/components/PostPreviewModal.vue";
import type { SinglePage, SinglePageRequest } from "@halo-dev/api-client";
import { computed, onMounted, ref } from "vue";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import cloneDeep from "lodash.clonedeep";
import { useRouter } from "vue-router";
import { randomUUID } from "@/utils/id";

const router = useRouter();

const initialFormState: SinglePageRequest = {
  page: {
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
      htmlMetas: [],
    },
    apiVersion: "content.halo.run/v1alpha1",
    kind: "SinglePage",
    metadata: {
      name: randomUUID(),
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
      formState.value.page.spec.slug = new Date().getTime().toString();
    }

    if (isUpdateMode.value) {
      // Get latest single page
      const { data: latestSinglePage } =
        await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage(
          {
            name: formState.value.page.metadata.name,
          }
        );

      formState.value.page = latestSinglePage;

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

const handlePublish = async () => {
  try {
    publishing.value = true;

    // Set rendered content
    formState.value.content.content = formState.value.content.raw;

    if (isUpdateMode.value) {
      const { headSnapshot } = formState.value.page.spec;
      const { name: singlePageName } = formState.value.page.metadata;
      const { data: latestContent } =
        await apiClient.content.updateSnapshotContent({
          snapshotName: headSnapshot as string,
          contentRequest: {
            raw: formState.value.content.raw as string,
            content: formState.value.content.content as string,
            rawType: formState.value.content.rawType as string,
            headSnapshotName: headSnapshot,
            subjectRef: {
              kind: "SinglePage",
              version: "v1alpha1",
              group: "content.halo.run",
              name: singlePageName,
            },
          },
        });

      // Get latest single page
      const { data: latestSinglePage } =
        await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage(
          {
            name: formState.value.page.metadata.name,
          }
        );

      formState.value.page = latestSinglePage;
      formState.value.page.spec.publish = true;
      formState.value.page.spec.headSnapshot = latestContent.snapshotName;
      formState.value.page.spec.releaseSnapshot =
        formState.value.page.spec.headSnapshot;

      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: singlePageName,
          singlePage: formState.value.page,
        }
      );
    } else {
      formState.value.page.spec.publish = true;
      await apiClient.singlePage.draftSinglePage({
        singlePageRequest: formState.value,
      });
    }

    router.push({ name: "SinglePages" });
  } catch (error) {
    console.error("Failed to publish single page", error);
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
  const { data } = await apiClient.content.obtainSnapshotContent({
    snapshotName: formState.value.page.spec.headSnapshot,
  });

  formState.value.content = data;
};

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
  }
});
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
      :owner="formState.page.spec.owner"
      :permalink="formState.page.status?.permalink"
      :publish-time="formState.page.spec.publishTime"
    />
  </div>
</template>
