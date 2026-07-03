<script lang="ts" setup>
import type { Menu, MenuItem, MenuItemTreeNode } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
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
import { utils } from "@halo-dev/ui-shared";
import { Draggable } from "@he-tree/vue";
import "@he-tree/vue/style/default.css";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { cloneDeep } from "es-toolkit";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import MenuItemEditingModal from "./components/MenuItemEditingModal.vue";
import MenuList from "./components/MenuList.vue";
import {
  buildMenuItemPositionRequest,
  flattenMenuItemTreeNodes,
  getMenuItemTreeNodeChildrenNames,
} from "./utils";

const { t } = useI18n();
const queryClient = useQueryClient();

const menuTreeItems = ref<MenuItemTreeNode[]>([]);
const previousMenuTreeItems = ref<MenuItemTreeNode[]>([]);
const selectedMenu = ref<Menu>();
const selectedMenuItem = ref<MenuItem>();
const selectedParentMenuItem = ref<MenuItem>();
const menuItemEditingModal = ref();

const {
  data: menuItemTree,
  isLoading,
  refetch,
} = useQuery<MenuItemTreeNode[]>({
  queryKey: ["menu-item-tree", selectedMenu],
  queryFn: async () => {
    const menuName = selectedMenu.value?.metadata.name;
    if (!menuName) {
      return [];
    }

    const { data } = await consoleApiClient.menuItem.listMenuItemTree({
      menuName,
    });
    return data;
  },
  onSuccess(data) {
    menuTreeItems.value = data;
    previousMenuTreeItems.value = cloneDeep(data);
  },
  refetchInterval(data) {
    const deletingMenuItems = flattenMenuItemTreeNodes(data || []).filter(
      (node) => !!node.menuItem.metadata.deletionTimestamp
    );
    return deletingMenuItems?.length ? 1000 : false;
  },
  enabled: computed(() => !!selectedMenu.value),
});

const handleOpenEditingModal = (node: MenuItemTreeNode) => {
  coreApiClient.menuItem
    .getMenuItem({
      name: node.menuItem.metadata.name,
    })
    .then((response) => {
      selectedMenuItem.value = response.data;
      menuItemEditingModal.value = true;
    });
};

const handleOpenCreateByParentModal = (node: MenuItemTreeNode) => {
  selectedParentMenuItem.value = node.menuItem;
  menuItemEditingModal.value = true;
};

const onMenuItemEditingModalClose = () => {
  selectedParentMenuItem.value = undefined;
  selectedMenuItem.value = undefined;
  menuItemEditingModal.value = false;
};

const onMenuItemSaved = async () => {
  if (!selectedMenu.value) {
    return;
  }

  await queryClient.invalidateQueries({ queryKey: ["menu-item-counts"] });
  await queryClient.invalidateQueries({ queryKey: ["menus"] });
  await refetch();
};

const positionUpdating = ref(false);

async function handleUpdatePosition() {
  if (positionUpdating.value) {
    return;
  }

  const selectedMenuName = selectedMenu.value?.metadata.name;
  if (!selectedMenuName) {
    return;
  }

  const positionRequest = buildMenuItemPositionRequest(
    previousMenuTreeItems.value,
    menuTreeItems.value
  );
  if (!positionRequest) {
    previousMenuTreeItems.value = cloneDeep(menuTreeItems.value);
    return;
  }

  try {
    positionUpdating.value = true;
    const { data } = await consoleApiClient.menuItem.updateMenuItemPosition({
      name: positionRequest.name,
      menuItemPositionRequest: {
        menuName: selectedMenuName,
        parentName: positionRequest.parentName,
        beforeName: positionRequest.beforeName,
      },
    });
    menuTreeItems.value = data;
    previousMenuTreeItems.value = cloneDeep(data);
  } catch (e) {
    console.error("Failed to update menu items", e);
    await refetch();
  } finally {
    await queryClient.invalidateQueries({ queryKey: ["menus"] });
    positionUpdating.value = false;
  }
}

const handleDelete = async (node: MenuItemTreeNode) => {
  Dialog.info({
    title: t("core.menu.operations.delete_menu_item.title"),
    description: t("core.menu.operations.delete_menu_item.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await coreApiClient.menuItem.deleteMenuItem({
        name: node.menuItem.metadata.name,
      });

      const childrenNames = getMenuItemTreeNodeChildrenNames(node);

      if (childrenNames.length) {
        const deleteChildrenRequests = childrenNames.map((name) =>
          coreApiClient.menuItem.deleteMenuItem({
            name,
          })
        );
        await Promise.all(deleteChildrenRequests);
      }

      await refetch();

      await queryClient.invalidateQueries({ queryKey: ["menu-item-counts"] });
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

function getMenuItemRefDisplayName(node: MenuItemTreeNode) {
  const { kind } = node.menuItem.spec.targetRef || {};

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
    :menu-item-tree="menuItemTree || []"
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
          <Transition v-else-if="!menuItemTree?.length" appear name="fade">
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
                'cursor-progress opacity-60': positionUpdating,
              }"
              :disable-drag="positionUpdating"
              trigger-class="drag-element"
              :indent="40"
              @after-drop="handleUpdatePosition"
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
                          {{
                            node.menuItem.status?.displayName ||
                            node.menuItem.spec.displayName
                          }}
                        </span>
                        <VTag v-if="getMenuItemRefDisplayName(node)">
                          {{ getMenuItemRefDisplayName(node) }}
                        </VTag>
                      </div>
                      <a
                        v-if="node.menuItem.status?.href"
                        :href="node.menuItem.status?.href"
                        :title="node.menuItem.status?.href"
                        target="_blank"
                        class="truncate text-xs text-gray-500 group-hover:text-gray-900"
                      >
                        {{ node.menuItem.status?.href }}
                      </a>
                    </div>
                  </div>
                  <div class="flex flex-none items-center gap-6">
                    <VStatusDot
                      v-if="node.menuItem.metadata.deletionTimestamp"
                      v-tooltip="$t('core.common.status.deleting')"
                      state="warning"
                      animate
                    />
                    <VDropdown
                      v-if="utils.permission.has(['system:menus:manage'])"
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
