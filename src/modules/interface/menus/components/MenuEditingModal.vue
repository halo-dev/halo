<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import type { Menu } from "@halo-dev/api-client";
import { v4 as uuid } from "uuid";
import type { PropType } from "vue";
import { computed, ref, watch } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import { submitForm } from "@formkit/core";
import cloneDeep from "lodash.clonedeep";
import { useMagicKeys } from "@vueuse/core";

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  menu: {
    type: Object as PropType<Menu | null>,
    default: null,
  },
});

const emit = defineEmits(["update:visible", "close"]);

const initialFormState: Menu = {
  spec: {
    displayName: "",
    // @ts-ignore
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
      await apiClient.extension.menu.updatev1alpha1Menu(
        formState.value.metadata.name,
        formState.value
      );
    } else {
      await apiClient.extension.menu.createv1alpha1Menu(formState.value);
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

watch(props, (newVal) => {
  const { Command_Enter } = useMagicKeys();
  let keyboardWatcher;
  if (newVal.visible) {
    keyboardWatcher = watch(Command_Enter, (v) => {
      if (v) {
        submitForm("menu-form");
      }
    });
  } else {
    keyboardWatcher?.unwatch();
  }

  if (newVal.visible && props.menu) {
    formState.value = cloneDeep(props.menu);
    return;
  }
  formState.value = cloneDeep(initialFormState);
  formState.value.metadata.name = uuid();
});
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
      :classes="{ form: 'w-full' }"
      type="form"
      @submit="handleCreateMenu"
    >
      <FormKit
        v-model="formState.spec.displayName"
        help="可根据此名称查询菜单项"
        label="菜单名称"
        type="text"
        validation="required"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="$formkit.submit('menu-form')">
          提交 ⌘ + ↵
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
