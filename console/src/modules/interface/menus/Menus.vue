<script lang="ts" setup>
import {
  IconAddCircle,
  IconListSettings,
  Dialog,
  VButton,
  VCard,
  VEmpty,
  VPageHeader,
  VSpace,
  VLoading,
  Toast,
} from "@halo-dev/components";
import MenuItemEditingModal from "./components/MenuItemEditingModal.vue";
import MenuItemListItem from "./components/MenuItemListItem.vue";
import MenuList from "./components/MenuList.vue";
import { computed, ref } from "vue";
import { apiClient } from "@/utils/api-client";
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
import { useI18n } from "vue-i18n";
import { useQuery, useQueryClient } from "@tanstack/vue-query";

const { t } = useI18n();
const queryClient = useQueryClient();

const menuTreeItems = ref<MenuTreeItem[]>([] as MenuTreeItem[]);
const selectedMenu = ref<Menu>();
const selectedMenuItem = ref<MenuItem>();
const selectedParentMenuItem = ref<MenuItem>();
const menuItemEditingModal = ref();

const {
  data: menuItems,
  isLoading,
  refetch,
} = useQuery<MenuItem[]>({
  queryKey: ["menu-items", selectedMenu],
  queryFn: async () => {
    if (!selectedMenu.value?.spec.menuItems) {
      return [];
    }

    const menuItemNames = selectedMenu.value.spec.menuItems.filter(Boolean);
    const { data } = await apiClient.extension.menuItem.listv1alpha1MenuItem({
      page: 0,
      size: 0,
      fieldSelector: [`name=(${menuItemNames.join(",")})`],
    });

    return data.items;
  },
  onSuccess(data) {
    menuTreeItems.value = buildMenuItemsTree(data);
  },
  refetchInterval(data) {
    const deletingMenuItems = data?.filter(
      (menuItem) => !!menuItem.metadata.deletionTimestamp
    );
    return deletingMenuItems?.length ? 3000 : false;
  },
  enabled: computed(() => !!selectedMenu.value),
});

const handleOpenEditingModal = (menuItem: MenuTreeItem) => {
  apiClient.extension.menuItem
    .getv1alpha1MenuItem({
      name: menuItem.metadata.name,
    })
    .then((response) => {
      selectedMenuItem.value = response.data;
      menuItemEditingModal.value = true;
    });
};

const handleOpenCreateByParentModal = (menuItem: MenuTreeItem) => {
  selectedParentMenuItem.value = convertMenuTreeItemToMenuItem(menuItem);
  menuItemEditingModal.value = true;
};

const onMenuItemEditingModalClose = () => {
  selectedParentMenuItem.value = undefined;
  selectedMenuItem.value = undefined;
};

const onMenuItemSaved = async (menuItem: MenuItem) => {
  const menuToUpdate = cloneDeep(selectedMenu.value);

  // update menu items
  if (
    menuToUpdate &&
    !menuToUpdate.spec.menuItems?.includes(menuItem.metadata.name)
  ) {
    menuToUpdate.spec.menuItems = [
      ...(menuToUpdate.spec.menuItems || []),
      menuItem.metadata.name,
    ];

    await apiClient.extension.menu.updatev1alpha1Menu({
      name: menuToUpdate.metadata.name,
      menu: menuToUpdate,
    });
  }

  await queryClient.invalidateQueries({ queryKey: ["menus"] });
  await refetch();
};

const handleUpdateInBatch = useDebounceFn(async () => {
  const menuTreeItemsToUpdate = resetMenuItemsTreePriority(menuTreeItems.value);
  const menuItemsToUpdate = convertTreeToMenuItems(menuTreeItemsToUpdate);
  try {
    const promises = menuItemsToUpdate.map((menuItem) =>
      apiClient.extension.menuItem.updatev1alpha1MenuItem({
        name: menuItem.metadata.name,
        menuItem,
      })
    );
    await Promise.all(promises);
  } catch (e) {
    console.error("Failed to update menu items", e);
  } finally {
    await queryClient.invalidateQueries({ queryKey: ["menus"] });
    await refetch();
  }
}, 300);

const handleDelete = async (menuItem: MenuTreeItem) => {
  Dialog.info({
    title: t("core.menu.operations.delete_menu_item.title"),
    description: t("core.menu.operations.delete_menu_item.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await apiClient.extension.menuItem.deletev1alpha1MenuItem({
        name: menuItem.metadata.name,
      });

      const childrenNames = getChildrenNames(menuItem);

      if (childrenNames.length) {
        const deleteChildrenRequests = childrenNames.map((name) =>
          apiClient.extension.menuItem.deletev1alpha1MenuItem({
            name,
          })
        );
        await Promise.all(deleteChildrenRequests);
      }

      await refetch();

      // update items under menu
      const menuToUpdate = cloneDeep(selectedMenu.value);
      if (menuToUpdate) {
        menuToUpdate.spec.menuItems = menuToUpdate.spec.menuItems?.filter(
          (name) => ![menuItem.metadata.name, ...childrenNames].includes(name)
        );
        await apiClient.extension.menu.updatev1alpha1Menu({
          name: menuToUpdate.metadata.name,
          menu: menuToUpdate,
        });
      }

      await queryClient.invalidateQueries({ queryKey: ["menus"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};
</script>
<template>
  <MenuItemEditingModal
    v-model:visible="menuItemEditingModal"
    :menu-item="selectedMenuItem"
    :parent-menu-item="selectedParentMenuItem"
    :menu="selectedMenu"
    @close="onMenuItemEditingModalClose"
    @saved="onMenuItemSaved"
  />
  <VPageHeader :title="$t('core.menu.title')">
    <template #icon>
      <IconListSettings class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <div class="flex flex-col gap-4 sm:flex-row">
      <div class="w-96">
        <MenuList v-model:selected-menu="selectedMenu" @select="refetch()" />
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
                      v-permission="['system:menus:manage']"
                      size="xs"
                      type="default"
                      @click="menuItemEditingModal = true"
                    >
                      {{ $t("core.common.buttons.new") }}
                    </VButton>
                  </VSpace>
                </div>
              </div>
            </div>
          </template>
          <VLoading v-if="isLoading" />
          <Transition v-else-if="!menuItems?.length" appear name="fade">
            <VEmpty
              :message="$t('core.menu.menu_item_empty.message')"
              :title="$t('core.menu.menu_item_empty.title')"
            >
              <template #actions>
                <VSpace>
                  <VButton @click="refetch()">
                    {{ $t("core.common.buttons.refresh") }}
                  </VButton>
                  <VButton
                    v-permission="['system:menus:manage']"
                    type="primary"
                    @click="menuItemEditingModal = true"
                  >
                    <template #icon>
                      <IconAddCircle class="h-full w-full" />
                    </template>
                    {{ $t("core.common.buttons.new") }}
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <MenuItemListItem
              :menu-tree-items="menuTreeItems"
              @change="handleUpdateInBatch"
              @delete="handleDelete"
              @open-editing="handleOpenEditingModal"
              @open-create-by-parent="handleOpenCreateByParentModal"
            />
          </Transition>
        </VCard>
      </div>
    </div>
  </div>
</template>
