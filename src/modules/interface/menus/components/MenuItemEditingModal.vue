<script lang="ts" setup>
import { VButton, VModal, VSpace } from "@halo-dev/components";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { computed, ref, watch } from "vue";
import type { Menu, MenuItem } from "@halo-dev/api-client";
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

const handleSaveMenuItem = async () => {
  try {
    saving.value = true;

    const menuItemSource = menuItemSources.find(
      (source) => source.value === selectedMenuItemSource.value
    );

    if (menuItemSource) {
      const { ref } = menuItemSource;
      if (ref) {
        formState.value.spec[ref] = {
          version: "content.halo.run/v1alpha1",
          kind: menuItemSource.kind,
          name: selectedRef.value as string,
        };
      }
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
  selectedMenuItemSource.value = menuItemSources[0].value;
  selectedRef.value = "";
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
        selectedRef.value = "";
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
      const { postRef, categoryRef, tagRef, singlePageRef } =
        formState.value.spec;

      if (postRef) {
        selectedMenuItemSource.value = "post";
        selectedRef.value = postRef.name;
      }

      if (categoryRef) {
        selectedMenuItemSource.value = "category";
        selectedRef.value = categoryRef.name;
      }

      if (tagRef) {
        selectedMenuItemSource.value = "tag";
        selectedRef.value = tagRef.name;
      }

      if (singlePageRef) {
        selectedMenuItemSource.value = "singlePage";
        selectedRef.value = singlePageRef.name;
      }
    } else {
      handleResetForm();
    }
  }
);

// MenuItem Ref
interface MenuItemSource {
  label: string;
  value: string;
  ref?: "postRef" | "categoryRef" | "tagRef" | "singlePageRef";
  kind?: "Post" | "Category" | "Tag" | "SinglePage";
}

const menuItemSources: MenuItemSource[] = [
  {
    label: "自定义链接",
    value: "custom",
  },
  {
    label: "文章",
    value: "post",
    ref: "postRef",
    kind: "Post",
  },
  {
    label: "自定义页面",
    value: "singlePage",
    ref: "singlePageRef",
    kind: "SinglePage",
  },
  {
    label: "分类",
    value: "category",
    ref: "categoryRef",
    kind: "Post",
  },
  {
    label: "标签",
    value: "tag",
    ref: "tagRef",
    kind: "Tag",
  },
];

const selectedMenuItemSource = ref<string>(menuItemSources[0].value);

const selectedRef = ref<string>("");

const onMenuItemSourceChange = () => {
  selectedRef.value = "";
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="编辑菜单项"
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
        v-if="!isUpdateMode && menu"
        v-model="selectedParentMenuItem"
        label="上级菜单项"
        placeholder="选择上级菜单项"
        type="menuItemSelect"
        :menu-items="menu?.spec.menuItems || []"
      />

      <FormKit
        v-model="selectedMenuItemSource"
        :options="menuItemSources"
        :disabled="isUpdateMode"
        label="类型"
        type="select"
        @change="onMenuItemSourceChange"
      />

      <FormKit
        v-if="selectedMenuItemSource === 'custom'"
        id="displayNameInput"
        v-model="formState.spec.displayName"
        label="名称"
        type="text"
        name="displayName"
        validation="required|length:0,100"
      />

      <FormKit
        v-if="selectedMenuItemSource === 'custom'"
        v-model="formState.spec.href"
        label="链接地址"
        type="text"
        name="href"
        validation="required|length:0,1024"
      />

      <FormKit
        v-if="selectedMenuItemSource === 'post'"
        v-model="selectedRef"
        placeholder="请选择文章"
        label="文章"
        type="postSelect"
        validation="required"
      />

      <FormKit
        v-if="selectedMenuItemSource === 'singlePage'"
        v-model="selectedRef"
        label="自定义页面"
        type="singlePageSelect"
        validation="required"
      />

      <FormKit
        v-if="selectedMenuItemSource === 'tag'"
        v-model="selectedRef"
        placeholder="请选择标签"
        label="标签"
        type="tagSelect"
        validation="required"
      />

      <FormKit
        v-if="selectedMenuItemSource === 'category'"
        v-model="selectedRef"
        placeholder="请选择分类"
        label="分类"
        type="categorySelect"
        validation="required"
      />
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          v-if="visible"
          type="secondary"
          @submit="$formkit.submit('menuitem-form')"
        >
        </SubmitButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
