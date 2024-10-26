<script lang="ts" setup>
// core libs
import { coreApiClient } from "@halo-dev/api-client";
import { computed, nextTick, onMounted, ref } from "vue";

// components
import SubmitButton from "@/components/button/SubmitButton.vue";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import { setFocus } from "@/formkit/utils/focus";
import { FormType } from "@/types/slug";
import useSlugify from "@console/composables/use-slugify";
import { useThemeCustomTemplates } from "@console/modules/interface/themes/composables/use-theme";
import { reset, submitForm } from "@formkit/core";
import type { Category } from "@halo-dev/api-client";
import {
  IconRefreshLine,
  Toast,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    category?: Category;
    parentCategory?: Category;
    isChildLevelCategory?: boolean;
  }>(),
  {
    category: undefined,
    parentCategory: undefined,
    isChildLevelCategory: false,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const queryClient = useQueryClient();
const { t } = useI18n();

const formState = ref<Category>({
  spec: {
    displayName: "",
    slug: "",
    description: "",
    cover: "",
    template: "",
    postTemplate: "",
    priority: 0,
    children: [],
    preventParentPostCascadeQuery: false,
  },
  status: {},
  apiVersion: "content.halo.run/v1alpha1",
  kind: "Category",
  metadata: {
    name: "",
    generateName: "category-",
  },
});
const selectedParentCategory = ref();
const saving = ref(false);
const modal = ref<InstanceType<typeof VModal> | null>(null);
const keepAddingSubmit = ref(false);

const isUpdateMode = !!props.category;

const modalTitle = props.category
  ? t("core.post_category.editing_modal.titles.update")
  : t("core.post_category.editing_modal.titles.create");

const annotationsFormRef = ref<InstanceType<typeof AnnotationsForm>>();

const handleSaveCategory = async () => {
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
    if (isUpdateMode) {
      await coreApiClient.content.category.updateCategory({
        name: formState.value.metadata.name,
        category: formState.value,
      });
    } else {
      // Gets parent category, calculates priority and updates it.
      let parentCategory: Category | undefined = undefined;

      if (selectedParentCategory.value) {
        const { data } = await coreApiClient.content.category.getCategory({
          name: selectedParentCategory.value,
        });
        parentCategory = data;
      }

      formState.value.spec.priority = parentCategory?.spec.children
        ? parentCategory.spec.children.length + 1
        : 0;

      const { data: createdCategory } =
        await coreApiClient.content.category.createCategory({
          category: formState.value,
        });

      if (parentCategory) {
        await coreApiClient.content.category.patchCategory({
          name: selectedParentCategory.value,
          jsonPatchInner: [
            {
              op: "add",
              path: "/spec/children",
              value: Array.from(
                new Set([
                  ...(parentCategory.spec.children || []),
                  createdCategory.metadata.name,
                ])
              ),
            },
          ],
        });
      }
    }

    if (keepAddingSubmit.value) {
      reset("category-form");
    } else {
      modal.value?.close();
    }

    queryClient.invalidateQueries({ queryKey: ["post-categories"] });

    Toast.success(t("core.common.toast.save_success"));
  } catch (e) {
    console.error("Failed to create category", e);
  } finally {
    saving.value = false;
  }
};

const handleSubmit = (keepAdding = false) => {
  keepAddingSubmit.value = keepAdding;
  submitForm("category-form");
};

onMounted(() => {
  if (props.category) {
    formState.value = cloneDeep(props.category);
  }
  selectedParentCategory.value = props.parentCategory?.metadata.name;
  setFocus("displayNameInput");
});

// custom templates
const { templates } = useThemeCustomTemplates("category");
const { templates: postTemplates } = useThemeCustomTemplates("post");

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
  computed(() => !isUpdateMode),
  FormType.CATEGORY
);
</script>
<template>
  <VModal ref="modal" :title="modalTitle" :width="700" @close="emit('close')">
    <FormKit
      id="category-form"
      type="form"
      name="category-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSaveCategory"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="md:col-span-1">
            <div class="sticky top-0">
              <span class="text-base font-medium text-gray-900">
                {{ $t("core.post_category.editing_modal.groups.general") }}
              </span>
            </div>
          </div>
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              v-if="!isUpdateMode"
              v-model="selectedParentCategory"
              type="categorySelect"
              :label="
                $t('core.post_category.editing_modal.fields.parent.label')
              "
            ></FormKit>
            <FormKit
              id="displayNameInput"
              v-model="formState.spec.displayName"
              name="displayName"
              :label="
                $t('core.post_category.editing_modal.fields.display_name.label')
              "
              type="text"
              validation="required|length:0,50"
            ></FormKit>
            <FormKit
              v-model="formState.spec.slug"
              :help="$t('core.post_category.editing_modal.fields.slug.help')"
              name="slug"
              :label="$t('core.post_category.editing_modal.fields.slug.label')"
              type="text"
              validation="required|length:0,50"
            >
              <template #suffix>
                <div
                  v-tooltip="
                    $t(
                      'core.post_category.editing_modal.fields.slug.refresh_message'
                    )
                  "
                  class="group flex h-full cursor-pointer items-center border-l px-3 transition-all hover:bg-gray-100"
                  @click="handleGenerateSlug(true)"
                >
                  <IconRefreshLine
                    class="h-4 w-4 text-gray-500 group-hover:text-gray-700"
                  />
                </div>
              </template>
            </FormKit>
            <FormKit
              v-model="formState.spec.template"
              :options="templates"
              :label="
                $t('core.post_category.editing_modal.fields.template.label')
              "
              :help="
                $t('core.post_category.editing_modal.fields.template.help')
              "
              type="select"
              name="template"
            ></FormKit>
            <FormKit
              v-model="formState.spec.postTemplate"
              :options="postTemplates"
              :label="
                $t(
                  'core.post_category.editing_modal.fields.post_template.label'
                )
              "
              :help="
                $t('core.post_category.editing_modal.fields.post_template.help')
              "
              type="select"
              name="postTemplate"
            ></FormKit>
            <FormKit
              v-model="formState.spec.cover"
              :help="$t('core.post_category.editing_modal.fields.cover.help')"
              name="cover"
              :label="$t('core.post_category.editing_modal.fields.cover.label')"
              type="attachment"
              :accepts="['image/*']"
              validation="length:0,1024"
            ></FormKit>
            <FormKit
              v-model="formState.spec.hideFromList"
              :disabled="isChildLevelCategory"
              :label="
                $t(
                  'core.post_category.editing_modal.fields.hide_from_list.label'
                )
              "
              :help="
                $t(
                  'core.post_category.editing_modal.fields.hide_from_list.help'
                )
              "
              type="checkbox"
              name="hideFromList"
            ></FormKit>
            <FormKit
              v-model="formState.spec.preventParentPostCascadeQuery"
              :label="
                $t(
                  'core.post_category.editing_modal.fields.prevent_parent_post_cascade_query.label'
                )
              "
              :help="
                $t(
                  'core.post_category.editing_modal.fields.prevent_parent_post_cascade_query.help'
                )
              "
              type="checkbox"
              name="preventParentPostCascadeQuery"
            ></FormKit>
            <FormKit
              v-model="formState.spec.description"
              name="description"
              :help="
                $t('core.post_category.editing_modal.fields.description.help')
              "
              :label="
                $t('core.post_category.editing_modal.fields.description.label')
              "
              type="textarea"
              validation="length:0,200"
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
            {{ $t("core.post_category.editing_modal.groups.annotations") }}
          </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Category"
          group="content.halo.run"
        />
      </div>
    </div>

    <template #footer>
      <div class="flex justify-between">
        <VSpace>
          <SubmitButton
            :loading="saving && !keepAddingSubmit"
            :disabled="saving && keepAddingSubmit"
            type="secondary"
            :text="$t('core.common.buttons.submit')"
            @submit="handleSubmit"
          >
          </SubmitButton>
          <VButton
            v-if="!isUpdateMode"
            :loading="saving && keepAddingSubmit"
            :disabled="saving && !keepAddingSubmit"
            @click="handleSubmit(true)"
          >
            {{ $t("core.common.buttons.save_and_continue") }}
          </VButton>
        </VSpace>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </div>
    </template>
  </VModal>
</template>
