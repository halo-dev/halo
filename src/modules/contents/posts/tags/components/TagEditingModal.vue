<script lang="ts" setup>
// core libs
import { computed, ref, watch, watchEffect } from "vue";
import { apiClient } from "@/utils/api-client";

// components
import {
  IconArrowLeft,
  IconArrowRight,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";

// types
import type { Tag } from "@halo-dev/api-client";

// libs
import cloneDeep from "lodash.clonedeep";
import { reset, submitForm } from "@formkit/core";
import { setFocus } from "@/formkit/utils/focus";
import { useMagicKeys } from "@vueuse/core";
import { v4 as uuid } from "uuid";

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

const initialFormState: Tag = {
  spec: {
    displayName: "",
    slug: "",
    color: "#b16cBe",
    cover: "",
  },
  apiVersion: "content.halo.run/v1alpha1",
  kind: "Tag",
  metadata: {
    name: uuid(),
  },
};

const formState = ref<Tag>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑文章标签" : "新增文章标签";
});

const handleSaveTag = async () => {
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
  formState.value.metadata.name = uuid();
  reset("tag-form");
};

const { Command_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter.value && props.visible) {
    submitForm("tag-form");
  }
});

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
</script>
<template>
  <VModal
    :title="modalTitle"
    :visible="visible"
    :width="600"
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
      name="tag-form"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleSaveTag"
    >
      <FormKit
        id="displayNameInput"
        v-model="formState.spec.displayName"
        label="名称"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="formState.spec.slug"
        help="通常作为标签访问地址标识"
        label="别名"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="formState.spec.color"
        help="需要主题适配以支持"
        label="颜色"
        type="color"
      ></FormKit>
      <FormKit
        v-model="formState.spec.cover"
        help="需要主题适配以支持"
        label="封面图"
        type="text"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton
          :loading="saving"
          type="secondary"
          @click="$formkit.submit('tag-form')"
        >
          保存
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
