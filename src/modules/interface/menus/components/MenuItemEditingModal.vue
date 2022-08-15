<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { computed, ref, watch, watchEffect } from "vue";
import type { MenuItem } from "@halo-dev/api-client";
import { v4 as uuid } from "uuid";
import { apiClient } from "@halo-dev/admin-shared";
import { reset, submitForm } from "@formkit/core";
import cloneDeep from "lodash.clonedeep";
import { useMagicKeys } from "@vueuse/core";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    menuItem: MenuItem | null;
  }>(),
  {
    visible: false,
    menuItem: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", menuItem: MenuItem): void;
}>();

const initialFormState: MenuItem = {
  spec: {
    displayName: "",
    href: "",
    children: new Set([]),
    priority: 0,
  },
  apiVersion: "v1alpha1",
  kind: "MenuItem",
  metadata: {
    name: uuid(),
  },
};

const formState = ref<MenuItem>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const handleSaveMenuItem = async () => {
  try {
    saving.value = true;

    // TODO 需要后端设置为 Array
    // @ts-ignore
    formState.value.spec.children = Array.from(formState.value.spec.children);

    if (isUpdateMode.value) {
      const { data } =
        await apiClient.extension.menuItem.updatev1alpha1MenuItem(
          formState.value.metadata.name,
          formState.value
        );
      onVisibleChange(false);
      emit("saved", data);
    } else {
      const { data } =
        await apiClient.extension.menuItem.createv1alpha1MenuItem(
          formState.value
        );
      onVisibleChange(false);
      emit("saved", data);
    }
  } catch (e) {
    console.error("Failed to create menu item", e);
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

watch(props, (newVal) => {
  if (newVal.visible && props.menuItem) {
    formState.value = cloneDeep(props.menuItem);
    return;
  }
  formState.value = cloneDeep(initialFormState);
  formState.value.metadata.name = uuid();
  reset("menuitem-form");
});

watchEffect(() => {
  let keyboardWatcher;
  const { Command_Enter } = useMagicKeys();
  if (props.visible) {
    keyboardWatcher = watch(Command_Enter, (v) => {
      if (v) {
        submitForm("menuitem-form");
      }
    });
  } else {
    keyboardWatcher?.unwatch();
  }
});
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="编辑菜单项"
    @update:visible="onVisibleChange"
  >
    <FormKit id="menuitem-form" type="form" @submit="handleSaveMenuItem">
      <FormKit
        v-model="formState.spec.displayName"
        label="名称"
        type="text"
        validation="required"
      ></FormKit>
      <FormKit
        v-model="formState.spec.href"
        label="链接地址"
        type="text"
        validation="required"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="$formkit.submit('menuitem-form')">
          提交 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
