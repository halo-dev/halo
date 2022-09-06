<script lang="ts" setup>
// core libs
import { computed, ref, watch, watchEffect } from "vue";
import { apiClient } from "@halo-dev/admin-shared";

// components
import { VButton, VModal, VSpace } from "@halo-dev/components";

// types
import type { Category } from "@halo-dev/api-client";

// libs
import cloneDeep from "lodash.clonedeep";
import { reset, submitForm } from "@formkit/core";
import { useMagicKeys } from "@vueuse/core";
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
    description: undefined,
    cover: undefined,
    template: undefined,
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

const { Command_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value && props.visible) {
    submitForm("category-form");
  }
});

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
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
    <FormKit id="category-form" type="form" @submit="handleSaveCategory">
      <FormKit
        v-model="formState.spec.displayName"
        label="名称"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="formState.spec.slug"
        help="通常作为分类访问地址标识"
        label="别名"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit label="上级目录" type="select"></FormKit>
      <FormKit
        v-model="formState.spec.cover"
        help="需要主题适配以支持"
        label="封面图"
        type="text"
      ></FormKit>
      <FormKit
        v-model="formState.spec.description"
        help="需要主题适配以支持"
        label="描述"
        type="textarea"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="$formkit.submit('category-form')">
          保存 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
