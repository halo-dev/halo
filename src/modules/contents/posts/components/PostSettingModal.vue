<script lang="ts" setup>
import { VButton, VModal, VSpace, VTabItem, VTabs } from "@halo-dev/components";
import { computed, ref, watchEffect } from "vue";
import type { Post } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { useThemeCustomTemplates } from "@/modules/interface/themes/composables/use-theme";
import { postLabels } from "@/constants/labels";
import { randomUUID } from "@/utils/id";
import { toDatetimeLocal, toISOString } from "@/utils/date";

const initialFormState: Post = {
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
  },
};

const props = withDefaults(
  defineProps<{
    visible: boolean;
    post?: Post;
    publishSupport?: boolean;
    onlyEmit?: boolean;
  }>(),
  {
    visible: false,
    post: undefined,
    publishSupport: true,
    onlyEmit: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", post: Post): void;
  (event: "published", post: Post): void;
}>();

const activeTab = ref("general");
const formState = ref<Post>(cloneDeep(initialFormState));
const saving = ref(false);
const publishing = ref(false);
const publishCanceling = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    setTimeout(() => {
      activeTab.value = "general";
    }, 200);
    emit("close");
  }
};

const handleSave = async () => {
  if (props.onlyEmit) {
    emit("saved", formState.value);
    return;
  }

  try {
    saving.value = true;

    const { data } = isUpdateMode.value
      ? await apiClient.extension.post.updatecontentHaloRunV1alpha1Post({
          name: formState.value.metadata.name,
          post: formState.value,
        })
      : await apiClient.extension.post.createcontentHaloRunV1alpha1Post({
          post: formState.value,
        });

    formState.value = data;
    emit("saved", data);

    handleVisibleChange(false);
  } catch (e) {
    console.error("Failed to save post", e);
  } finally {
    saving.value = false;
  }
};

const handlePublish = async () => {
  if (props.onlyEmit) {
    emit("published", formState.value);
    return;
  }

  try {
    publishing.value = true;

    const { data } = await apiClient.post.publishPost({
      name: formState.value.metadata.name,
    });

    formState.value = data;

    emit("published", data);

    handleVisibleChange(false);
  } catch (e) {
    console.error("Failed to publish post", e);
  } finally {
    publishing.value = false;
  }
};

const handleUnpublish = async () => {
  try {
    publishCanceling.value = true;

    await apiClient.post.unpublishPost({
      name: formState.value.metadata.name,
    });

    handleVisibleChange(false);
  } catch (e) {
    console.error("Failed to publish post", e);
  } finally {
    publishCanceling.value = false;
  }
};

watchEffect(() => {
  if (props.post) {
    formState.value = cloneDeep(props.post);
  }
});

// custom templates
const { templates } = useThemeCustomTemplates("post");

// publishTime convert
const publishTime = computed(() => {
  const { publishTime } = formState.value.spec;
  if (publishTime) {
    console.log(toDatetimeLocal(publishTime));
    return toDatetimeLocal(publishTime);
  }
  return "";
});

const onPublishTimeChange = (value: string) => {
  formState.value.spec.publishTime = toISOString(value);
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="700"
    title="文章设置"
    :centered="false"
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
            v-model="formState.spec.title"
            label="标题"
            type="text"
            name="title"
            validation="required|length:0,100"
          ></FormKit>
          <FormKit
            v-model="formState.spec.slug"
            label="别名"
            name="slug"
            type="text"
            validation="required|length:0,100"
          ></FormKit>
          <FormKit
            v-model="formState.spec.categories"
            label="分类目录"
            name="categories"
            type="categoryCheckbox"
          />
          <FormKit
            v-model="formState.spec.tags"
            label="标签"
            name="tags"
            type="tagCheckbox"
          />
          <FormKit
            v-model="formState.spec.excerpt.autoGenerate"
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
            v-if="!formState.spec.excerpt.autoGenerate"
            v-model="formState.spec.excerpt.raw"
            label="自定义摘要"
            name="raw"
            type="textarea"
            :rows="5"
            validation="length:0,1024"
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
            v-model="formState.spec.allowComment"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="允许评论"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.spec.pinned"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="是否置顶"
            name="pinned"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.spec.visible"
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
            :value="publishTime"
            label="发表时间"
            type="datetime-local"
            @input="onPublishTimeChange"
          ></FormKit>
          <FormKit
            v-model="formState.spec.template"
            :options="templates"
            label="自定义模板"
            name="template"
            type="select"
          ></FormKit>
          <FormKit
            v-model="formState.spec.cover"
            name="cover"
            label="封面图"
            type="attachment"
            validation="length:0,1024"
          ></FormKit>
        </FormKit>
        <!--TODO: add SEO/Metas/Inject Code form-->
      </VTabItem>
    </VTabs>

    <template #footer>
      <VSpace>
        <template v-if="publishSupport">
          <VButton
            v-if="formState.metadata.labels?.[postLabels.PUBLISHED] !== 'true'"
            :loading="publishing"
            type="secondary"
            @click="handlePublish()"
          >
            发布
          </VButton>
          <VButton
            v-else
            :loading="publishCanceling"
            type="danger"
            @click="handleUnpublish()"
          >
            取消发布
          </VButton>
        </template>
        <VButton :loading="saving" type="secondary" @click="handleSave">
          保存
        </VButton>
        <VButton type="default" @click="handleVisibleChange(false)">
          关闭
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
