<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { computed, ref, watch } from "vue";
import type { Menu, MenuItem, Ref } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { reset } from "@formkit/core";
import cloneDeep from "lodash.clonedeep";
import { setFocus } from "@/formkit/utils/focus";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    menu?: Menu;
    parentMenuItem: MenuItem;
    menuItem?: MenuItem;
  }>(),
  {
    visible: false,
    menu: undefined,
    parentMenuItem: undefined,
    menuItem: undefined,
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
    children: [],
    priority: 0,
  },
  apiVersion: "v1alpha1",
  kind: "MenuItem",
  metadata: {
    name: "",
    generateName: "menu-item-",
  },
};

const selectedParentMenuItem = ref<string>("");
const formState = ref<MenuItem>(cloneDeep(initialFormState));
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑菜单项" : "新增菜单项";
});

const handleSaveMenuItem = async () => {
  try {
    saving.value = true;

    const menuItemRef = menuItemRefs.find(
      (ref) => ref.ref?.kind === selectedRefKind.value
    );

    if (menuItemRef?.ref) {
      formState.value.spec.targetRef = {
        ...menuItemRef.ref,
        name: selectedRefName.value,
      };
      formState.value.spec.displayName = undefined;
      formState.value.spec.href = undefined;
    }

    if (isUpdateMode.value) {
      const { data } =
        await apiClient.extension.menuItem.updatev1alpha1MenuItem({
          name: formState.value.metadata.name,
          menuItem: formState.value,
        });

      onVisibleChange(false);
      emit("saved", data);
    } else {
      const { data } =
        await apiClient.extension.menuItem.createv1alpha1MenuItem({
          menuItem: formState.value,
        });

      // if parent menu item is selected, add the new menu item to the parent menu item
      if (selectedParentMenuItem.value) {
        const { data: menuItemToUpdate } =
          await apiClient.extension.menuItem.getv1alpha1MenuItem({
            name: selectedParentMenuItem.value,
          });

        menuItemToUpdate.spec.children = [
          ...(menuItemToUpdate.spec.children || []),
          data.metadata.name,
        ];

        await apiClient.extension.menuItem.updatev1alpha1MenuItem({
          name: menuItemToUpdate.metadata.name,
          menuItem: menuItemToUpdate,
        });
      }

      onVisibleChange(false);
      emit("saved", data);
    }

    Toast.success("保存成功");
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

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  selectedRefKind.value = "";
  selectedRefName.value = "";
  selectedParentMenuItem.value = "";
  reset("menuitem-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      selectedParentMenuItem.value = props.parentMenuItem?.metadata.name;
      setFocus("displayNameInput");

      if (!props.menuItem) {
        selectedRefName.value = "";
      }
    } else {
      handleResetForm();
    }
  }
);

watch(
  () => props.menuItem,
  (menuItem) => {
    if (menuItem) {
      formState.value = cloneDeep(menuItem);

      // Set Ref related
      const { targetRef } = formState.value.spec;

      if (!targetRef) {
        return;
      }

      selectedRefName.value = targetRef.name;
      selectedRefKind.value = targetRef.kind as string;
    } else {
      handleResetForm();
    }
  }
);

interface MenuItemRef {
  label: string;
  inputType?: string;
  ref?: Ref;
}

const baseRef: Ref = {
  group: "content.halo.run",
  version: "v1alpha1",
  name: "",
};

const menuItemRefs: MenuItemRef[] = [
  {
    label: "自定义链接",
  },
  {
    label: "文章",
    inputType: "postSelect",
    ref: {
      ...baseRef,
      kind: "Post",
    },
  },
  {
    label: "自定义页面",
    inputType: "singlePageSelect",
    ref: {
      ...baseRef,
      kind: "SinglePage",
    },
  },
  {
    label: "分类",
    inputType: "categorySelect",
    ref: {
      ...baseRef,
      kind: "Category",
    },
  },
  {
    label: "标签",
    inputType: "tagSelect",
    ref: {
      ...baseRef,
      kind: "Tag",
    },
  },
];

const menuItemRefsMap = menuItemRefs.map((menuItemRef) => {
  return {
    label: menuItemRef.label,
    value: menuItemRef.ref?.kind,
  };
});

const selectedRef = computed(() => {
  return menuItemRefs.find(
    (menuItemRef) => menuItemRef.ref?.kind === selectedRefKind.value
  );
});

const selectedRefKind = ref<string>("");
const selectedRefName = ref<string>("");

const onMenuItemSourceChange = () => {
  selectedRefName.value = "";
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    :title="modalTitle"
    @update:visible="onVisibleChange"
  >
    <FormKit
      id="menuitem-form"
      name="menuitem-form"
      type="form"
      :preserve="true"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSaveMenuItem"
    >
      <FormKit
        v-if="!isUpdateMode && menu && visible"
        v-model="selectedParentMenuItem"
        label="上级菜单项"
        placeholder="选择上级菜单项"
        type="menuItemSelect"
        :menu-items="menu?.spec.menuItems || []"
      />

      <FormKit
        v-model="selectedRefKind"
        :options="menuItemRefsMap"
        :disabled="isUpdateMode"
        label="类型"
        type="select"
        @change="onMenuItemSourceChange"
      />

      <FormKit
        v-if="!selectedRefKind"
        id="displayNameInput"
        v-model="formState.spec.displayName"
        label="名称"
        type="text"
        name="displayName"
        validation="required|length:0,100"
      />

      <FormKit
        v-if="!selectedRefKind"
        v-model="formState.spec.href"
        label="链接地址"
        type="text"
        name="href"
        validation="required|length:0,1024"
      />

      <FormKit
        v-if="selectedRef?.ref"
        :id="selectedRef.inputType"
        :key="selectedRef.inputType"
        v-model="selectedRefName"
        :placeholder="`请选择${selectedRef.label}`"
        :label="selectedRef.label"
        :type="selectedRef.inputType"
        validation="required"
      />
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          :loading="saving"
          type="secondary"
          @submit="$formkit.submit('menuitem-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
