<script lang="ts" setup>
import {
  IconRefreshLine,
  Toast,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { computed, nextTick, ref, watchEffect } from "vue";
import type { SinglePage } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { useThemeCustomTemplates } from "@/modules/interface/themes/composables/use-theme";
import { singlePageLabels } from "@/constants/labels";
import { randomUUID } from "@/utils/id";
import { toDatetimeLocal, toISOString } from "@/utils/date";
import { submitForm } from "@formkit/core";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import useSlugify from "@/composables/use-slugify";
import { useMutation } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const initialFormState: SinglePage = {
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
  },
};

const props = withDefaults(
  defineProps<{
    visible: boolean;
    singlePage?: SinglePage;
    publishSupport?: boolean;
    onlyEmit?: boolean;
  }>(),
  {
    visible: false,
    singlePage: undefined,
    publishSupport: true,
    onlyEmit: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", singlePage: SinglePage): void;
  (event: "published", singlePage: SinglePage): void;
}>();

const { t } = useI18n();

const formState = ref<SinglePage>(cloneDeep(initialFormState));
const saving = ref(false);
const publishing = ref(false);
const publishCanceling = ref(false);
const submitType = ref<"publish" | "save">();

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const annotationsFormRef = ref<InstanceType<typeof AnnotationsForm>>();

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
    submitForm("singlePage-setting-form");
  });
};

const handlePublishClick = () => {
  submitType.value = "publish";

  nextTick(() => {
    submitForm("singlePage-setting-form");
  });
};

// Fix me:
// Force update post settings,
// because currently there may be errors caused by changes in version due to asynchronous processing.
const { mutateAsync: singlePageUpdateMutate } = useMutation({
  mutationKey: ["singlePage-update"],
  mutationFn: async (page: SinglePage) => {
    const { data: latestSinglePage } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: page.metadata.name,
      });

    return apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
      {
        name: page.metadata.name,
        singlePage: {
          ...latestSinglePage,
          spec: page.spec,
          metadata: {
            ...latestSinglePage.metadata,
            annotations: page.metadata.annotations,
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
      ? await singlePageUpdateMutate(formState.value)
      : await apiClient.extension.singlePage.createcontentHaloRunV1alpha1SinglePage(
          {
            singlePage: formState.value,
          }
        );

    formState.value = data;
    emit("saved", data);

    onVisibleChange(false);

    Toast.success(t("core.common.toast.save_success"));
  } catch (error) {
    console.error("Failed to save single page", error);
  } finally {
    saving.value = false;
  }
};

const handlePublish = async () => {
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
    emit("published", formState.value);
    return;
  }

  try {
    publishing.value = true;

    const singlePageToUpdate = cloneDeep(formState.value);

    singlePageToUpdate.spec.releaseSnapshot =
      singlePageToUpdate.spec.headSnapshot;
    singlePageToUpdate.spec.publish = true;

    const { data } =
      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: formState.value.metadata.name,
          singlePage: singlePageToUpdate,
        }
      );

    formState.value = data;

    emit("published", data);

    onVisibleChange(false);

    Toast.success(t("core.common.toast.publish_success"));
  } catch (error) {
    console.error("Failed to publish single page", error);
  } finally {
    publishing.value = false;
  }
};

const handleUnpublish = async () => {
  try {
    publishCanceling.value = true;

    const { data: singlePage } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: formState.value.metadata.name,
      });

    const singlePageToUpdate = cloneDeep(singlePage);
    singlePageToUpdate.spec.publish = false;

    const { data } =
      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: formState.value.metadata.name,
          singlePage: singlePageToUpdate,
        }
      );

    formState.value = data;

    onVisibleChange(false);

    Toast.success(t("core.common.toast.cancel_publish_success"));
  } catch (error) {
    console.error("Failed to unpublish single page", error);
  } finally {
    publishCanceling.value = false;
  }
};

watchEffect(() => {
  if (props.singlePage) {
    formState.value = cloneDeep(props.singlePage);
  }
});

// custom templates
const { templates } = useThemeCustomTemplates("page");

// publishTime
const publishTime = computed({
  get() {
    const { publishTime } = formState.value.spec;
    return publishTime ? toDatetimeLocal(publishTime) : undefined;
  },
  set(value) {
    formState.value.spec.publishTime = value ? toISOString(value) : undefined;
  },
});

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
    :title="$t('core.page.settings.title')"
    :centered="false"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>

    <FormKit
      id="singlePage-setting-form"
      type="form"
      name="singlePage-setting-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSubmit"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                {{ $t("core.page.settings.groups.general") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              v-model="formState.spec.title"
              :label="$t('core.page.settings.fields.title.label')"
              type="text"
              name="title"
              validation="required|length:0,100"
            ></FormKit>
            <FormKit
              v-model="formState.spec.slug"
              :label="$t('core.page.settings.fields.slug.label')"
              name="slug"
              type="text"
              validation="required|length:0,100"
              :help="$t('core.page.settings.fields.slug.help')"
            >
              <template #suffix>
                <div
                  v-tooltip="
                    $t('core.page.settings.fields.slug.refresh_message')
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
              v-model="formState.spec.excerpt.autoGenerate"
              :options="[
                { label: $t('core.common.radio.yes'), value: true },
                { label: $t('core.common.radio.no'), value: false },
              ]"
              name="autoGenerate"
              :label="
                $t('core.page.settings.fields.auto_generate_excerpt.label')
              "
              type="radio"
            >
            </FormKit>
            <FormKit
              v-if="!formState.spec.excerpt.autoGenerate"
              v-model="formState.spec.excerpt.raw"
              name="raw"
              :label="$t('core.page.settings.fields.raw_excerpt.label')"
              type="textarea"
              validation="length:0,1024"
              :rows="5"
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
                {{ $t("core.page.settings.groups.advanced") }}
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
              name="allowComment"
              :label="$t('core.page.settings.fields.allow_comment.label')"
              type="radio"
            ></FormKit>
            <FormKit
              v-model="formState.spec.pinned"
              :options="[
                { label: $t('core.common.radio.yes'), value: true },
                { label: $t('core.common.radio.no'), value: false },
              ]"
              :label="$t('core.page.settings.fields.pinned.label')"
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
              :label="$t('core.page.settings.fields.visible.label')"
              name="visible"
              type="select"
            ></FormKit>
            <FormKit
              v-model="publishTime"
              :label="$t('core.page.settings.fields.publish_time.label')"
              type="datetime-local"
              name="publishTime"
            ></FormKit>
            <FormKit
              v-model="formState.spec.template"
              :options="templates"
              :label="$t('core.page.settings.fields.template.label')"
              type="select"
              name="template"
            ></FormKit>
            <FormKit
              v-model="formState.spec.cover"
              :label="$t('core.page.settings.fields.cover.label')"
              type="attachment"
              name="cover"
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
            {{ $t("core.page.settings.groups.annotations") }}
          </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="SinglePage"
          group="content.halo.run"
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <template v-if="publishSupport">
          <VButton
            v-if="
              formState.metadata.labels?.[singlePageLabels.PUBLISHED] !== 'true'
            "
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
        <VButton :loading="saving" type="secondary" @click="handleSaveClick">
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton type="default" @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.close") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
