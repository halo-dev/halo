<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import type { Menu } from "@halo-dev/api-client";
import { v4 as uuid } from "uuid";
import { computed, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import { reset } from "@formkit/core";
import cloneDeep from "lodash.clonedeep";
import { setFocus } from "@/formkit/utils/focus";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    menu: Menu | null;
  }>(),
  {
    visible: false,
    menu: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "created", menu: Menu): void;
}>();

const initialFormState: Menu = {
  spec: {
    displayName: "",
    menuItems: [],
  },
  apiVersion: "v1alpha1",
  kind: "Menu",
  metadata: {
    name: uuid(),
  },
};

const formState = ref<Menu>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const handleCreateMenu = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await apiClient.extension.menu.updatev1alpha1Menu({
        name: formState.value.metadata.name,
        menu: formState.value,
      });
    } else {
      const { data } = await apiClient.extension.menu.createv1alpha1Menu({
        menu: formState.value,
      });
      emit("created", data);
    }
    onVisibleChange(false);
  } catch (e) {
    console.error("Failed to create menu", e);
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
  reset("menu-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      setFocus("menuDisplayNameInput");
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.menu,
  (menu) => {
    if (menu) {
      formState.value = cloneDeep(menu);
    } else {
      handleResetForm();
    }
  }
);
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="编辑菜单"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="menu-form"
      name="menu-form"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleCreateMenu"
    >
      <FormKit
        id="menuDisplayNameInput"
        v-model="formState.spec.displayName"
        help="可根据此名称查询菜单项"
        label="菜单名称"
        type="text"
        name="displayName"
        validation="required"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          type="secondary"
          @submit="$formkit.submit('menu-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
