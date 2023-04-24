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
import { useMutation } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const initialFormState: Post = {
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

const { t } = useI18n();

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
  submitType.value = "publish";

  nextTick(() => {
    submitForm("post-setting-form");
  });
};

// Fix me:
// Force update post settings,
// because currently there may be errors caused by changes in version due to asynchronous processing.
const { mutateAsync: postUpdateMutate } = useMutation({
  mutationKey: ["post-update"],
  mutationFn: async (post: Post) => {
    const { data: latestPost } =
      await apiClient.extension.post.getcontentHaloRunV1alpha1Post({
        name: post.metadata.name,
      });

    return apiClient.extension.post.updatecontentHaloRunV1alpha1Post(
      {
        name: post.metadata.name,
        post: {
          ...latestPost,
          spec: post.spec,
          metadata: {
            ...latestPost.metadata,
            annotations: post.metadata.annotations,
          },
        },
      },
      {
        mute: true,
      }
    );
  },
  retry: 3,
  onError: (error) => {
    console.error("Failed to update post", error);
    Toast.error(t("core.common.toast.server_internal_error"));
  },
});

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
      ? await postUpdateMutate(formState.value)
      : await apiClient.extension.post.createcontentHaloRunV1alpha1Post({
          post: formState.value,
        });

    formState.value = data;
    emit("saved", data);

    handleVisibleChange(false);

    Toast.success(t("core.common.toast.save_success"));
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

    Toast.success(t("core.common.toast.publish_success"));
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

    Toast.success(t("core.common.toast.cancel_publish_success"));
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
const publishTime = computed({
  get() {
    const { publishTime } = formState.value.spec;
    return publishTime ? toDatetimeLocal(publishTime) : undefined;
  },
  set(value) {
    formState.value.spec.publishTime = value ? toISOString(value) : undefined;
  },
});

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
    :title="$t('core.post.settings.title')"
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
                {{ $t("core.post.settings.groups.general") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              v-model="formState.spec.title"
              :label="$t('core.post.settings.fields.title.label')"
              type="text"
              name="title"
              validation="required|length:0,100"
            ></FormKit>
            <FormKit
              v-model="formState.spec.slug"
              :label="$t('core.post.settings.fields.slug.label')"
              name="slug"
              type="text"
              validation="required|length:0,100"
              :help="$t('core.post.settings.fields.slug.help')"
            >
              <template #suffix>
                <div
                  v-tooltip="
                    $t('core.post.settings.fields.slug.refresh_message')
                  "
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
              :label="$t('core.post.settings.fields.categories.label')"
              name="categories"
              type="categorySelect"
              :multiple="true"
            />
            <FormKit
              v-model="formState.spec.tags"
              :label="$t('core.post.settings.fields.tags.label')"
              name="tags"
              type="tagSelect"
              :multiple="true"
            />
            <FormKit
              v-model="formState.spec.excerpt.autoGenerate"
              :options="[
                { label: $t('core.common.radio.yes'), value: true },
                { label: $t('core.common.radio.no'), value: false },
              ]"
              name="autoGenerate"
              :label="
                $t('core.post.settings.fields.auto_generate_excerpt.label')
              "
              type="radio"
            >
            </FormKit>
            <FormKit
              v-if="!formState.spec.excerpt.autoGenerate"
              v-model="formState.spec.excerpt.raw"
              :label="$t('core.post.settings.fields.raw_excerpt.label')"
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
                {{ $t("core.post.settings.groups.advanced") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              v-model="formState.spec.allowComment"
              :options="[
                { label: $t('core.common.radio.yes'), value: true },
                { label: $t('core.common.radio.no'), value: false },
              ]"
              :label="$t('core.post.settings.fields.allow_comment.label')"
              type="radio"
            ></FormKit>
            <FormKit
              v-model="formState.spec.pinned"
              :options="[
                { label: $t('core.common.radio.yes'), value: true },
                { label: $t('core.common.radio.no'), value: false },
              ]"
              :label="$t('core.post.settings.fields.pinned.label')"
              name="pinned"
              type="radio"
            ></FormKit>
            <FormKit
              v-model="formState.spec.visible"
              :options="[
                { label: $t('core.common.select.public'), value: 'PUBLIC' },
                {
                  label: $t('core.common.select.private'),
                  value: 'PRIVATE',
                },
              ]"
              :label="$t('core.post.settings.fields.visible.label')"
              name="visible"
              type="select"
            ></FormKit>
            <FormKit
              v-model="publishTime"
              :label="$t('core.post.settings.fields.publish_time.label')"
              type="datetime-local"
            ></FormKit>
            <FormKit
              v-model="formState.spec.template"
              :options="templates"
              :label="$t('core.post.settings.fields.template.label')"
              name="template"
              type="select"
            ></FormKit>
            <FormKit
              v-model="formState.spec.cover"
              name="cover"
              :label="$t('core.post.settings.fields.cover.label')"
              type="attachment"
              :accepts="['image/*']"
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
          <span class="text-base font-medium text-gray-900">
            {{ $t("core.post.settings.groups.annotations") }}
          </span>
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
            {{ $t("core.common.buttons.publish") }}
          </VButton>
          <VButton
            v-else
            :loading="publishCanceling"
            type="danger"
            @click="handleUnpublish()"
          >
            {{ $t("core.common.buttons.cancel_publish") }}
          </VButton>
        </template>
        <VButton :loading="saving" type="secondary" @click="handleSaveClick()">
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton type="default" @click="handleVisibleChange(false)">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
