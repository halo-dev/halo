<script lang="ts" setup>
import {
  IconRefreshLine,
  Toast,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { computed, nextTick, ref, watchEffect } from "vue";
import type { Post } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { useThemeCustomTemplates } from "@/modules/interface/themes/composables/use-theme";
import { postLabels } from "@/constants/labels";
import { randomUUID } from "@/utils/id";
import { toDatetimeLocal, toISOString } from "@/utils/date";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import { submitForm } from "@formkit/core";
import useSlugify from "@/composables/use-slugify";

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

const formState = ref<Post>(cloneDeep(initialFormState));
const saving = ref(false);
const publishing = ref(false);
const publishCanceling = ref(false);
const submitType = ref<"publish" | "save">();

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleSubmit = () => {
  if (submitType.value === "publish") {
    handlePublish();
  }
  if (submitType.value === "save") {
    handleSave();
  }
};

const handleSaveClick = () => {
  submitType.value = "save";

  nextTick(() => {
    submitForm("post-setting-form");
  });
};

const handlePublishClick = () => {
  if (!props.onlyEmit) {
    handlePublish();
  }

  submitType.value = "publish";

  nextTick(() => {
    submitForm("post-setting-form");
  });
};

const handleSave = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();

  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } =
    annotationsFormRef.value || {};

  if (customFormInvalid || specFormInvalid) {
    return;
  }

  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };

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

    Toast.success("保存成功");
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

    Toast.success("发布成功");
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

    Toast.success("取消发布成功");
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
    return toDatetimeLocal(publishTime);
  }
  return "";
});

const onPublishTimeChange = (value: string) => {
  formState.value.spec.publishTime = value ? toISOString(value) : undefined;
};

const annotationsFormRef = ref<InstanceType<typeof AnnotationsForm>>();

// slug
const { handleGenerateSlug } = useSlugify(
  computed(() => formState.value.spec.title),
  computed({
    get() {
      return formState.value.spec.slug;
    },
    set(value) {
      formState.value.spec.slug = value;
    },
  }),
  computed(() => !isUpdateMode.value)
);
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

    <FormKit
      id="post-setting-form"
      type="form"
      name="post-setting-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSubmit"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                常规设置
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
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
              help="通常用于生成文章的固定链接"
            >
              <template #suffix>
                <div
                  v-tooltip="'根据标题重新生成别名'"
                  class="group flex h-full cursor-pointer items-center border-l px-3 transition-all hover:bg-gray-100"
                  @click="handleGenerateSlug"
                >
                  <IconRefreshLine
                    class="h-4 w-4 text-gray-500 group-hover:text-gray-700"
                  />
                </div>
              </template>
            </FormKit>
            <FormKit
              v-model="formState.spec.categories"
              label="分类目录"
              name="categories"
              type="categorySelect"
              :multiple="true"
            />
            <FormKit
              v-model="formState.spec.tags"
              label="标签"
              name="tags"
              type="tagSelect"
              :multiple="true"
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
          </div>
        </div>

        <div class="py-5">
          <div class="border-t border-gray-200"></div>
        </div>

        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                高级设置
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
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
                { label: '私有', value: 'PRIVATE' },
              ]"
              label="可见性"
              name="visible"
              type="select"
            ></FormKit>
            <FormKit
              :model-value="publishTime"
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
          </div>
        </div>
      </div>
    </FormKit>

    <div class="py-5">
      <div class="border-t border-gray-200"></div>
    </div>

    <div class="md:grid md:grid-cols-4 md:gap-6">
      <div class="md:col-span-1">
        <div class="sticky top-0">
          <span class="text-base font-medium text-gray-900"> 元数据 </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Post"
          group="content.halo.run"
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <template v-if="publishSupport">
          <VButton
            v-if="formState.metadata.labels?.[postLabels.PUBLISHED] !== 'true'"
            :loading="publishing"
            type="secondary"
            @click="handlePublishClick()"
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
        <VButton :loading="saving" type="secondary" @click="handleSaveClick()">
          保存
        </VButton>
        <VButton type="default" @click="handleVisibleChange(false)">
          关闭
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
