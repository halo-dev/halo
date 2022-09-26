<script lang="ts" setup>
// core libs
import { computed, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";

// components
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";

// types
import type { Category } from "@halo-dev/api-client";

// libs
import cloneDeep from "lodash.clonedeep";
import { reset } from "@formkit/core";
import { setFocus } from "@/formkit/utils/focus";
import { v4 as uuid } from "uuid";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    category: Category | null;
  }>(),
  {
    visible: false,
    category: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const initialFormState: Category = {
  spec: {
    displayName: "",
    slug: "",
    description: "",
    cover: "",
    template: "",
    priority: 0,
    children: [],
  },
  status: {},
  apiVersion: "content.halo.run/v1alpha1",
  kind: "Category",
  metadata: {
    name: uuid(),
  },
};

const formState = ref<Category>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑文章分类" : "新增文章分类";
});

const handleSaveCategory = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await apiClient.extension.category.updatecontentHaloRunV1alpha1Category({
        name: formState.value.metadata.name,
        category: formState.value,
      });
    } else {
      await apiClient.extension.category.createcontentHaloRunV1alpha1Category({
        category: formState.value,
      });
    }
    onVisibleChange(false);
  } catch (e) {
    console.error("Failed to create category", e);
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
  formState.value.metadata.name = uuid();
  reset("category-form");
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
  () => props.category,
  (category) => {
    if (category) {
      formState.value = cloneDeep(category);
    } else {
      handleResetForm();
    }
  }
);
</script>
<template>
  <VModal
    :title="modalTitle"
    :visible="visible"
    :width="600"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="category-form"
      name="category-form"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSaveCategory"
    >
      <FormKit
        id="displayNameInput"
        v-model="formState.spec.displayName"
        name="displayName"
        label="名称"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="formState.spec.slug"
        help="通常作为分类访问地址标识"
        name="slug"
        label="别名"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="formState.spec.cover"
        help="需要主题适配以支持"
        name="cover"
        label="封面图"
        type="text"
      ></FormKit>
      <FormKit
        v-model="formState.spec.description"
        name="description"
        help="需要主题适配以支持"
        label="描述"
        type="textarea"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          @submit="$formkit.submit('category-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
