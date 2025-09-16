<script lang="ts" setup>
import { usePermission } from "@/utils/permission";
import type { Menu, MenuItem } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconList,
  IconListSettings,
  IconMore,
  Toast,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { Draggable } from "@he-tree/vue";
import "@he-tree/vue/style/default.css";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import MenuItemEditingModal from "./components/MenuItemEditingModal.vue";
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
const { currentUserHasPermission } = usePermission();

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

async function handleUpdateInBatch() {
  if (batchUpdating.value) {
    return;
  }

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
}

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

const TargetRef = {
  Post: t("core.menu.menu_item_editing_modal.fields.ref_kind.options.post"),
  SinglePage: t(
    "core.menu.menu_item_editing_modal.fields.ref_kind.options.single_page"
  ),
  Category: t(
    "core.menu.menu_item_editing_modal.fields.ref_kind.options.category"
  ),
  Tag: t("core.menu.menu_item_editing_modal.fields.ref_kind.options.tag"),
};

function getMenuItemRefDisplayName(menuItem: MenuTreeItem) {
  const { kind } = menuItem.spec.targetRef || {};

  if (kind && TargetRef[kind]) {
    return TargetRef[kind];
  }

  return undefined;
}
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
      <IconListSettings />
    </template>
  </VPageHeader>
  <div class="m-0 md:m-4">
    <div class="flex flex-col gap-4 sm:flex-row">
      <div class="w-96 flex-none">
        <MenuList v-model:selected-menu="selectedMenu" />
      </div>
      <div class="min-w-0 flex-1 shrink">
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
                      <IconAddCircle />
                    </template>
                    {{ $t("core.common.buttons.new") }}
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <Draggable
              v-model="menuTreeItems"
              :class="{
                'cursor-progress opacity-60': batchUpdating,
              }"
              :disable-drag="batchUpdating"
              trigger-class="drag-element"
              :indent="40"
              @after-drop="handleUpdateInBatch"
            >
              <template #default="{ node }">
                <div
                  class="group relative flex w-full items-center justify-between px-4 py-3 hover:bg-gray-50"
                >
                  <div class="min-w-0 flex-1 shrink">
                    <div
                      v-permission="['system:menus:manage']"
                      class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
                    >
                      <IconList class="h-3.5 w-3.5" />
                    </div>
                    <div class="flex flex-col gap-1">
                      <div class="inline-flex items-center gap-2">
                        <span
                          class="truncate text-sm font-medium text-gray-900"
                        >
                          {{ node.status.displayName }}
                        </span>
                        <VTag v-if="getMenuItemRefDisplayName(node)">
                          {{ getMenuItemRefDisplayName(node) }}
                        </VTag>
                      </div>
                      <a
                        v-if="node.status?.href"
                        :href="node.status?.href"
                        :title="node.status?.href"
                        target="_blank"
                        class="truncate text-xs text-gray-500 group-hover:text-gray-900"
                      >
                        {{ node.status.href }}
                      </a>
                    </div>
                  </div>
                  <div class="flex flex-none items-center gap-6">
                    <VStatusDot
                      v-if="node.metadata.deletionTimestamp"
                      v-tooltip="$t('core.common.status.deleting')"
                      state="warning"
                      animate
                    />
                    <VDropdown
                      v-if="currentUserHasPermission(['system:menus:manage'])"
                    >
                      <div
                        class="cursor-pointer rounded p-1 transition-all hover:text-blue-600 group-hover:bg-gray-200/60"
                        @click.stop
                      >
                        <IconMore />
                      </div>
                      <template #popper>
                        <VDropdownItem @click="handleOpenEditingModal(node)">
                          {{ $t("core.common.buttons.edit") }}
                        </VDropdownItem>
                        <VDropdownItem
                          @click="handleOpenCreateByParentModal(node)"
                        >
                          {{
                            $t("core.menu.operations.add_sub_menu_item.button")
                          }}
                        </VDropdownItem>
                        <VDropdownItem
                          type="danger"
                          @click="handleDelete(node)"
                        >
                          {{ $t("core.common.buttons.delete") }}
                        </VDropdownItem>
                      </template>
                    </VDropdown>
                  </div>
                </div>
              </template>
            </Draggable>
          </Transition>
        </VCard>
      </div>
    </div>
  </div>
</template>
<style scoped>
:deep(.vtlist-inner) {
  @apply divide-y divide-gray-100;
}
:deep(.he-tree-drag-placeholder) {
  height: 60px;
}
</style>
