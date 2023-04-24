<script lang="ts" setup>
// core libs
import { computed, nextTick, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";

// components
import {
  IconArrowLeft,
  IconArrowRight,
  IconRefreshLine,
  Toast,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";

// types
import type { Tag } from "@halo-dev/api-client";

// libs
import cloneDeep from "lodash.clonedeep";
import { reset } from "@formkit/core";
import { setFocus } from "@/formkit/utils/focus";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import useSlugify from "@/composables/use-slugify";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    tag: Tag | null;
  }>(),
  {
    visible: false,
    tag: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "previous"): void;
  (event: "next"): void;
}>();

const { t } = useI18n();

const initialFormState: Tag = {
  spec: {
    displayName: "",
    slug: "",
    color: "#ffffff",
    cover: "",
  },
  apiVersion: "content.halo.run/v1alpha1",
  kind: "Tag",
  metadata: {
    name: "",
    generateName: "tag-",
  },
};

const formState = ref<Tag>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value
    ? t("core.post_tag.editing_modal.titles.update")
    : t("core.post_tag.editing_modal.titles.create");
});

const annotationsFormRef = ref<InstanceType<typeof AnnotationsForm>>();

const handleSaveTag = async () => {
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

  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await apiClient.extension.tag.updatecontentHaloRunV1alpha1Tag({
        name: formState.value.metadata.name,
        tag: formState.value,
      });
    } else {
      await apiClient.extension.tag.createcontentHaloRunV1alpha1Tag({
        tag: formState.value,
      });
    }
    onVisibleChange(false);

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to create tag", e);
  } finally {
    saving.value = false;
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("tag-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("displayNameInput");
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.tag,
  (tag) => {
    if (tag) {
      formState.value = cloneDeep(tag);
    } else {
      handleResetForm();
    }
  }
);

// slug
const { handleGenerateSlug } = useSlugify(
  computed(() => formState.value.spec.displayName),
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
    :title="modalTitle"
    :visible="visible"
    :width="700"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <span @click="emit('previous')">
        <IconArrowLeft />
      </span>
      <span @click="emit('next')">
        <IconArrowRight />
      </span>
    </template>

    <FormKit
      id="tag-form"
      type="form"
      name="tag-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSaveTag"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                {{ $t("core.post_tag.editing_modal.groups.general") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              id="displayNameInput"
              v-model="formState.spec.displayName"
              name="displayName"
              :label="
                $t('core.post_tag.editing_modal.fields.display_name.label')
              "
              type="text"
              validation="required|length:0,50"
            ></FormKit>
            <FormKit
              v-model="formState.spec.slug"
              :help="$t('core.post_tag.editing_modal.fields.slug.help')"
              :label="$t('core.post_tag.editing_modal.fields.slug.label')"
              name="slug"
              type="text"
              validation="required|length:0,50"
            >
              <template #suffix>
                <div
                  v-tooltip="
                    $t(
                      'core.post_tag.editing_modal.fields.slug.refresh_message'
                    )
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
              v-model="formState.spec.color"
              name="color"
              :help="$t('core.post_tag.editing_modal.fields.color.help')"
              :label="$t('core.post_tag.editing_modal.fields.color.label')"
              type="color"
              validation="length:0,50"
            ></FormKit>
            <FormKit
              v-model="formState.spec.cover"
              name="cover"
              :help="$t('core.post_tag.editing_modal.fields.cover.help')"
              :label="$t('core.post_tag.editing_modal.fields.cover.label')"
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
            {{ $t("core.post_tag.editing_modal.groups.annotations") }}
          </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Tag"
          group="content.halo.run"
        />
      </div>
    </div>

    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('tag-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
