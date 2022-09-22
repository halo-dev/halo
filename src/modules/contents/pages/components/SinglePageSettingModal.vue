<script lang="ts" setup>
import { VButton, VModal, VSpace, VTabItem, VTabs } from "@halo-dev/components";
import { computed, ref, watchEffect } from "vue";
import type { SinglePageRequest } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { v4 as uuid } from "uuid";

const initialFormState: SinglePageRequest = {
  page: {
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

const props = withDefaults(
  defineProps<{
    visible: boolean;
    singlePage?: SinglePageRequest;
  }>(),
  {
    visible: false,
    singlePage: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", singlePage: SinglePageRequest): void;
}>();

const activeTab = ref("general");
const formState = ref<SinglePageRequest>(cloneDeep(initialFormState));
const saving = ref(false);
const publishing = ref(false);
const publishCanceling = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.page.metadata.creationTimestamp;
});

const onVisibleChange = (visible: boolean) => {
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
      const { data } = await apiClient.singlePage.updateDraftSinglePage({
        name: formState.value.page.metadata.name,
        singlePageRequest: formState.value,
      });
      formState.value.page = data;
      emit("saved", formState.value);
    } else {
      const { data } = await apiClient.singlePage.draftSinglePage({
        singlePageRequest: formState.value,
      });
      formState.value.page = data;
      emit("saved", formState.value);
    }
  } catch (error) {
    console.error("Failed to save single page", error);
  } finally {
    saving.value = false;
  }
};

const handlePublish = async () => {
  try {
    publishing.value = true;

    // Save single page
    await handleSave();

    // Get latest version single page
    const { data: latestData } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: formState.value.page.metadata.name,
      });
    formState.value.page = latestData;

    // Publish single page
    const { data } = await apiClient.singlePage.publishSinglePage({
      name: formState.value.page.metadata.name,
    });
    formState.value.page = data;
    emit("saved", formState.value);
  } catch (error) {
    console.error("Failed to publish single page", error);
  } finally {
    publishing.value = false;
  }
};

const handleCancelPublish = async () => {
  try {
    publishCanceling.value = true;

    // Update published spec = false
    const singlePageToUpdate = cloneDeep(formState.value);
    singlePageToUpdate.page.spec.published = false;
    await apiClient.singlePage.updateDraftSinglePage({
      name: singlePageToUpdate.page.metadata.name,
      singlePageRequest: singlePageToUpdate,
    });

    // Get latest version single page
    const { data: latestData } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: formState.value.page.metadata.name,
      });
    formState.value.page = latestData;
    emit("saved", formState.value);
  } catch (error) {
    console.error("Failed to cancel publish single page", error);
  } finally {
    publishCanceling.value = false;
  }
};

watchEffect(() => {
  if (props.singlePage) {
    formState.value = cloneDeep(props.singlePage);
  }
});
</script>

<template>
  <VModal
    :visible="visible"
    :width="700"
    title="页面设置"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>

    <VTabs v-model:active-id="activeTab" type="outline">
      <VTabItem id="general" label="常规">
        <FormKit id="basic" :actions="false" :preserve="true" type="form">
          <FormKit
            v-model="formState.page.spec.title"
            label="标题"
            type="text"
            validation="required"
          ></FormKit>
          <FormKit
            v-model="formState.page.spec.slug"
            label="别名"
            name="slug"
            type="text"
            validation="required"
          ></FormKit>
          <FormKit
            v-model="formState.page.spec.excerpt.autoGenerate"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="自动生成摘要"
            type="radio"
          >
          </FormKit>
          <FormKit
            v-if="!formState.page.spec.excerpt.autoGenerate"
            v-model="formState.page.spec.excerpt.raw"
            label="自定义摘要"
            type="textarea"
          ></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="advanced" label="高级">
        <FormKit id="advanced" :actions="false" :preserve="true" type="form">
          <FormKit
            v-model="formState.page.spec.allowComment"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="禁止评论"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.page.spec.pinned"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="是否置顶"
            name="pinned"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.page.spec.visible"
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
            v-model="formState.page.spec.publishTime"
            label="发表时间"
            type="datetime-local"
          ></FormKit>
          <FormKit
            v-model="formState.page.spec.template"
            label="自定义模板"
            type="text"
          ></FormKit>
          <FormKit
            v-model="formState.page.spec.cover"
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
            formState.page.status?.phase === "PUBLISHED" ? "重新发布" : "发布"
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
          v-if="formState.page.status?.phase === 'PUBLISHED'"
          :loading="publishCanceling"
          type="danger"
          size="sm"
          @click="handleCancelPublish"
        >
          取消发布
        </VButton>
        <VButton size="sm" type="default" @click="onVisibleChange(false)">
          关闭
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
