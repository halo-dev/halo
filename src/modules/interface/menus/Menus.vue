<script lang="ts" setup>
import {
  IconListSettings,
  useDialog,
  VButton,
  VCard,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import MenuItemEditingModal from "./components/MenuItemEditingModal.vue";
import MenuItemListItem from "./components/MenuItemListItem.vue";
import MenuList from "./components/MenuList.vue";
import { ref } from "vue";
import { apiClient } from "@halo-dev/admin-shared";
import type { Menu, MenuItem } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import type { MenuTreeItem } from "./utils";
import {
  buildMenuItemsTree,
  convertMenuTreeItemToMenuItem,
  convertTreeToMenuItems,
  getChildrenNames,
  resetMenuItemsTreePriority,
} from "./utils";
import { useDebounceFn } from "@vueuse/core";

const menuItems = ref<MenuItem[]>([] as MenuItem[]);
const menuTreeItems = ref<MenuTreeItem[]>([] as MenuTreeItem[]);
const selectedMenu = ref<Menu | undefined>();
const selectedMenuItem = ref<MenuItem | null>(null);
const menuListRef = ref();
const menuItemEditingModal = ref();

const dialog = useDialog();

const handleFetchMenuItems = async () => {
  try {
    if (!selectedMenu.value?.spec.menuItems) {
      return;
    }
    const menuItemNames = Array.from(selectedMenu.value.spec.menuItems)?.map(
      (item) => item
    );
    const { data } = await apiClient.extension.menuItem.listv1alpha1MenuItem(
      0,
      0,
      [],
      [`name=(${menuItemNames.join(",")})`]
    );
    menuItems.value = data.items;
    // Build the menu tree
    menuTreeItems.value = buildMenuItemsTree(data.items);
  } catch (e) {
    console.error("Failed to fetch menu items", e);
  }
};

const handleOpenEditingModal = (menuItem: MenuTreeItem) => {
  selectedMenuItem.value = convertMenuTreeItemToMenuItem(menuItem);
  menuItemEditingModal.value = true;
};

const onMenuItemSaved = async (menuItem: MenuItem) => {
  const menuToUpdate = cloneDeep(selectedMenu.value);

  if (menuToUpdate) {
    const menuItemsToUpdate = Array.from(
      cloneDeep(menuToUpdate.spec.menuItems) || new Set<string>()
    );
    menuItemsToUpdate.push(menuItem.metadata.name);

    // @ts-ignore
    menuToUpdate.spec.menuItems = Array.from(new Set(menuItemsToUpdate));
    await apiClient.extension.menu.updatev1alpha1Menu(
      menuToUpdate.metadata.name,
      menuToUpdate
    );
  }

  await menuListRef.value.handleFetchMenus();
  await handleFetchMenuItems();
};

const handleUpdateInBatch = useDebounceFn(async () => {
  const menuTreeItemsToUpdate = resetMenuItemsTreePriority(menuTreeItems.value);
  const menuItemsToUpdate = convertTreeToMenuItems(menuTreeItemsToUpdate);
  try {
    const promises = menuItemsToUpdate.map((menuItem) =>
      apiClient.extension.menuItem.updatev1alpha1MenuItem(
        menuItem.metadata.name,
        menuItem
      )
    );
    await Promise.all(promises);
  } catch (e) {
    console.log("Failed to update menu items", e);
  } finally {
    await menuListRef.value.handleFetchMenus();
    await handleFetchMenuItems();
  }
}, 500);

const handleDelete = async (menuItem: MenuTreeItem) => {
  dialog.info({
    title: "是否确定删除该菜单？",
    description: "删除后将无法恢复",
    confirmType: "danger",
    onConfirm: async () => {
      await apiClient.extension.menuItem.deletev1alpha1MenuItem(
        menuItem.metadata.name
      );

      const childrenNames = getChildrenNames(menuItem);

      if (childrenNames.length) {
        setTimeout(() => {
          dialog.info({
            title: "检查到当前菜单下包含子菜单，是否删除？",
            description: "如果选择否，那么所有子菜单将转移到一级菜单",
            confirmType: "danger",
            onConfirm: async () => {
              const promises = childrenNames.map((name) =>
                apiClient.extension.menuItem.deletev1alpha1MenuItem(name)
              );
              await Promise.all(promises);
            },
          });
        }, 200);
      }

      await menuListRef.value.handleFetchMenus();
      await handleFetchMenuItems();
    },
  });
};
</script>
<template>
  <MenuItemEditingModal
    v-model:visible="menuItemEditingModal"
    :menu-item="selectedMenuItem"
    @close="selectedMenuItem = null"
    @saved="onMenuItemSaved"
  />
  <VPageHeader title="菜单">
    <template #icon>
      <IconListSettings class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <div class="flex flex-col gap-4 sm:flex-row">
      <div class="w-96">
        <MenuList
          ref="menuListRef"
          v-model:selected-menu="selectedMenu"
          @select="handleFetchMenuItems"
        />
      </div>
      <div class="flex-1">
        <VCard :body-class="['!p-0']">
          <template #header>
            <div class="block w-full bg-gray-50 px-4 py-3">
              <div
                class="relative flex flex-col items-start sm:flex-row sm:items-center"
              >
                <div class="flex w-full flex-1 sm:w-auto">
                  <span class="text-base font-medium">
                    {{ selectedMenu?.spec.displayName }}
                  </span>
                </div>
                <div class="mt-4 flex sm:mt-0">
                  <VSpace>
                    <VButton
                      size="xs"
                      type="default"
                      @click="menuItemEditingModal = true"
                    >
                      新增
                    </VButton>
                  </VSpace>
                </div>
              </div>
            </div>
          </template>
          <MenuItemListItem
            :menu-tree-items="menuTreeItems"
            @change="handleUpdateInBatch"
            @delete="handleDelete"
            @open-editing="handleOpenEditingModal"
          />
        </VCard>
      </div>
    </div>
  </div>
</template>
