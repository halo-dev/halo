<script lang="ts" setup>
import type { Menu, MenuItem } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconListSettings,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useDebounceFn } from "@vueuse/core";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import MenuItemEditingModal from "./components/MenuItemEditingModal.vue";
import MenuItemListItem from "./components/MenuItemListItem.vue";
import MenuList from "./components/MenuList.vue";
import type { MenuTreeItem } from "./utils";
import {
  buildMenuItemsTree,
  convertMenuTreeItemToMenuItem,
  convertTreeToMenuItems,
  getChildrenNames,
  resetMenuItemsTreePriority,
} from "./utils";

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
    const { data } = await coreApiClient.menuItem.listMenuItem({
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
    return deletingMenuItems?.length ? 1000 : false;
  },
  enabled: computed(() => !!selectedMenu.value),
});

const handleOpenEditingModal = (menuItem: MenuTreeItem) => {
  coreApiClient.menuItem
    .getMenuItem({
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
  menuItemEditingModal.value = false;
};

const onMenuItemSaved = async (menuItem: MenuItem) => {
  if (!selectedMenu.value) {
    return;
  }

  // update menu items
  await coreApiClient.menu.patchMenu({
    name: selectedMenu.value.metadata.name,
    jsonPatchInner: [
      {
        op: "add",
        path: "/spec/menuItems",
        value: Array.from(
          new Set([
            ...(selectedMenu.value.spec.menuItems || []),
            menuItem.metadata.name,
          ])
        ),
      },
    ],
  });

  await queryClient.invalidateQueries({ queryKey: ["menus"] });
  await refetch();
};

const batchUpdating = ref(false);

const handleUpdateInBatch = useDebounceFn(async () => {
  const menuTreeItemsToUpdate = resetMenuItemsTreePriority(menuTreeItems.value);
  const menuItemsToUpdate = convertTreeToMenuItems(menuTreeItemsToUpdate);
  try {
    batchUpdating.value = true;
    const promises = menuItemsToUpdate.map((menuItem) =>
      coreApiClient.menuItem.patchMenuItem({
        name: menuItem.metadata.name,
        jsonPatchInner: [
          {
            op: "add",
            path: "/spec/priority",
            value: menuItem.spec.priority || 0,
          },
          {
            op: "add",
            path: "/spec/children",
            value: menuItem.spec.children || [],
          },
        ],
      })
    );
    await Promise.all(promises);
  } catch (e) {
    console.error("Failed to update menu items", e);
  } finally {
    await queryClient.invalidateQueries({ queryKey: ["menus"] });
    await refetch();
    batchUpdating.value = false;
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
      await coreApiClient.menuItem.deleteMenuItem({
        name: menuItem.metadata.name,
      });

      const childrenNames = getChildrenNames(menuItem);

      if (childrenNames.length) {
        const deleteChildrenRequests = childrenNames.map((name) =>
          coreApiClient.menuItem.deleteMenuItem({
            name,
          })
        );
        await Promise.all(deleteChildrenRequests);
      }

      await refetch();

      // update items under menu
      await coreApiClient.menu.patchMenu({
        name: selectedMenu.value?.metadata.name as string,
        jsonPatchInner: [
          {
            op: "add",
            path: "/spec/menuItems",
            value:
              selectedMenu.value?.spec.menuItems?.filter(
                (name) =>
                  ![menuItem.metadata.name, ...childrenNames].includes(name)
              ) || [],
          },
        ],
      });

      await queryClient.invalidateQueries({ queryKey: ["menus"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};
</script>
<template>
  <MenuItemEditingModal
    v-if="menuItemEditingModal && selectedMenu"
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
      <div class="w-96 flex-none">
        <MenuList v-model:selected-menu="selectedMenu" />
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
                    type="secondary"
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
              v-model="menuTreeItems"
              :class="{
                'cursor-progress opacity-60': batchUpdating,
              }"
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
