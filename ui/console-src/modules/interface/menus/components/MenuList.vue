<script lang="ts" setup>
import type {
  Menu,
  MenuItem,
  MenuItemV1alpha1ApiListMenuItemRequest,
  MenuV1alpha1ApiListMenuRequest,
} from "@halo-dev/api-client";
import {
  consoleApiClient,
  coreApiClient,
  paginate,
} from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VButton,
  VCard,
  VDropdownItem,
  VEmpty,
  VEntity,
  VEntityContainer,
  VEntityField,
  VLoading,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { cloneDeep } from "es-toolkit";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { buildMenuItemHierarchyPatch, resolveClonedParentName } from "../utils";
import MenuEditingModal from "./MenuEditingModal.vue";

interface SystemMenuConfig {
  primary: string;
}

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    selectedMenu?: Menu;
  }>(),
  {
    selectedMenu: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:selectedMenu", menu: Menu): void;
}>();

const selectedMenuToUpdate = ref<Menu>();
const menuEditingModal = ref<boolean>(false);

const {
  data: menus,
  isLoading,
  refetch,
} = useQuery<Menu[]>({
  queryKey: ["menus"],
  queryFn: async () => {
    return await paginate<MenuV1alpha1ApiListMenuRequest, Menu>(
      (params) => coreApiClient.menu.listMenu(params),
      {
        size: 1000,
      }
    );
  },
  onSuccess(data) {
    if (props.selectedMenu) {
      const updatedMenu = data?.find(
        (menu) => menu.metadata.name === props.selectedMenu?.metadata.name
      );
      if (updatedMenu) {
        emit("update:selectedMenu", updatedMenu);
      }
    }
  },
  refetchInterval(data) {
    const hasDeletingMenu = data?.some(
      (menu) => !!menu.metadata.deletionTimestamp
    );
    return hasDeletingMenu ? 1000 : false;
  },
});

const { data: menuItemCounts, refetch: refetchMenuItemCounts } = useQuery<
  Record<string, number>
>({
  queryKey: ["menu-item-counts"],
  queryFn: async () => {
    const items = await paginate<
      MenuItemV1alpha1ApiListMenuItemRequest,
      MenuItem
    >((params) => coreApiClient.menuItem.listMenuItem(params), {
      size: 1000,
    });

    return items.reduce<Record<string, number>>((counts, item) => {
      const menuName = item.spec.menuName;
      if (menuName) {
        counts[menuName] = (counts[menuName] || 0) + 1;
      }
      return counts;
    }, {});
  },
});

async function listMenuItemsByMenuName(menuName: string) {
  return await paginate<MenuItemV1alpha1ApiListMenuItemRequest, MenuItem>(
    (params) => coreApiClient.menuItem.listMenuItem(params),
    {
      fieldSelector: [`spec.menuName=${menuName}`],
      size: 1000,
    }
  );
}

const menuQuery = useRouteQuery("menu");
const handleSelect = (menu: Menu) => {
  emit("update:selectedMenu", menu);
  menuQuery.value = menu.metadata.name;
};

const handleCloneMenu = async (menu: Menu) => {
  Dialog.info({
    title: t("core.menu.operations.clone.title"),
    description: t("core.menu.operations.clone.description", {
      name: menu.spec.displayName,
    }),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        const originalItems = await listMenuItemsByMenuName(menu.metadata.name);

        const newMenu = cloneDeep(menu);
        newMenu.metadata.name = "";
        newMenu.metadata.generateName = "menu-";
        newMenu.spec.menuItems = [];

        const { data: createdMenu } = await coreApiClient.menu.createMenu({
          menu: newMenu,
        });

        if (originalItems.length) {
          const oldToNewNameMap = new Map<string, string>();

          const createNewItemPromises = originalItems.map((originalItem) => {
            const newItem = cloneDeep(originalItem);
            newItem.metadata.name = "";
            newItem.metadata.generateName = "menu-item-";
            newItem.spec.menuName = createdMenu.metadata.name;
            newItem.spec.parent = undefined;
            newItem.spec.children = undefined;
            return coreApiClient.menuItem
              .createMenuItem({ menuItem: newItem })
              .then((res) => {
                oldToNewNameMap.set(
                  originalItem.metadata.name,
                  res.data.metadata.name
                );
                return res.data;
              });
          });

          await Promise.all(createNewItemPromises);

          const patchPromises: Promise<unknown>[] = [];
          for (const originalItem of originalItems) {
            const newName = oldToNewNameMap.get(originalItem.metadata.name);
            const clonedParentName = resolveClonedParentName(
              originalItem,
              oldToNewNameMap
            );
            if (newName) {
              patchPromises.push(
                coreApiClient.menuItem.patchMenuItem({
                  name: newName,
                  jsonPatchInner: buildMenuItemHierarchyPatch(
                    {
                      ...originalItem,
                      spec: {
                        ...originalItem.spec,
                        menuName: createdMenu.metadata.name,
                        parent: clonedParentName,
                      },
                    },
                    createdMenu.metadata.name
                  ),
                })
              );
            }
          }
          await Promise.all(patchPromises);
        }

        Toast.success(t("core.common.toast.copy_success"));
        await refetch();
        await refetchMenuItemCounts();
      } catch (e) {
        console.error("Failed to clone menu", e);
        Toast.error((e as Error).message);
      }
    },
  });
};

const handleDeleteMenu = async (menu: Menu) => {
  Dialog.warning({
    title: t("core.menu.operations.delete_menu.title"),
    description: t("core.menu.operations.delete_menu.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await coreApiClient.menu.deleteMenu({
          name: menu.metadata.name,
        });

        const menuItems = await listMenuItemsByMenuName(menu.metadata.name);
        const deleteItemsPromises = menuItems.map((item) =>
          coreApiClient.menuItem.deleteMenuItem({
            name: item.metadata.name,
          })
        );

        await Promise.all(deleteItemsPromises);

        Toast.success(t("core.common.toast.delete_success"));
      } catch (e) {
        console.error("Failed to delete menu", e);
      } finally {
        await refetch();
        await refetchMenuItemCounts();
      }
    },
  });
};

const handleOpenEditingModal = (menu?: Menu) => {
  selectedMenuToUpdate.value = menu;
  menuEditingModal.value = true;
};

onMounted(async () => {
  await refetch();

  if (menuQuery.value) {
    const menu = menus.value?.find((m) => m.metadata.name === menuQuery.value);
    if (menu) {
      handleSelect(menu);
    }
    return;
  }

  if (menus.value?.length) {
    handleSelect(menus.value[0]);
  }
});

// primary menu
const { data: primaryMenuName, refetch: refetchPrimaryMenuName } = useQuery({
  queryKey: ["primary-menu-name"],
  queryFn: async () => {
    const { data } =
      await consoleApiClient.configMap.system.getSystemConfigByGroup({
        group: "menu",
      });

    const { primary } = (data as SystemMenuConfig) || {};

    return primary;
  },
});

const handleSetPrimaryMenu = async (menu: Menu) => {
  await consoleApiClient.configMap.system.updateSystemConfigByGroup({
    group: "menu",
    body: {
      primary: menu.metadata.name,
    },
  });

  await refetchPrimaryMenuName();

  Toast.success(t("core.menu.operations.set_primary.toast_success"));
};
</script>
<template>
  <MenuEditingModal
    v-if="menuEditingModal"
    :menu="selectedMenuToUpdate"
    @close="menuEditingModal = false"
    @created="handleSelect"
  />
  <VCard :body-class="['!p-0']" :title="$t('core.menu.title')">
    <VLoading v-if="isLoading" />
    <Transition v-else-if="!menus?.length" appear name="fade">
      <VEmpty
        :message="$t('core.menu.empty.message')"
        :title="$t('core.menu.empty.title')"
      >
        <template #actions>
          <VButton size="sm" @click="refetch()">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
        </template>
      </VEmpty>
    </Transition>
    <Transition v-else appear name="fade">
      <VEntityContainer>
        <VEntity
          v-for="menu in menus"
          :key="menu.metadata.name"
          :is-selected="selectedMenu?.metadata.name === menu.metadata.name"
          @click="handleSelect(menu)"
        >
          <template #start>
            <VEntityField
              :title="menu.spec?.displayName"
              :description="
                $t('core.menu.list.fields.items_count', {
                  count: menuItemCounts?.[menu.metadata.name] || 0,
                })
              "
            >
              <template v-if="menu.metadata.name === primaryMenuName" #extra>
                <VTag>
                  {{ $t("core.menu.list.fields.primary") }}
                </VTag>
              </template>
            </VEntityField>
          </template>
          <template #end>
            <VEntityField v-if="menu.metadata.deletionTimestamp">
              <template #description>
                <VStatusDot
                  v-tooltip="$t('core.common.status.deleting')"
                  state="warning"
                  animate
                />
              </template>
            </VEntityField>
          </template>
          <template
            v-if="utils.permission.has(['system:menus:manage'])"
            #dropdownItems
          >
            <VDropdownItem
              v-if="primaryMenuName !== menu.metadata.name"
              @click="handleSetPrimaryMenu(menu)"
            >
              {{ $t("core.menu.operations.set_primary.button") }}
            </VDropdownItem>
            <VDropdownItem @click="handleOpenEditingModal(menu)">
              {{ $t("core.common.buttons.edit") }}
            </VDropdownItem>
            <VDropdownItem @click="handleCloneMenu(menu)">
              {{ $t("core.common.buttons.copy") }}
            </VDropdownItem>
            <VDropdownItem
              :disabled="primaryMenuName === menu.metadata.name"
              type="danger"
              @click="handleDeleteMenu(menu)"
            >
              {{ $t("core.common.buttons.delete") }}
            </VDropdownItem>
          </template>
        </VEntity>
      </VEntityContainer>
    </Transition>
    <template v-if="utils.permission.has(['system:menus:manage'])" #footer>
      <VButton block type="secondary" @click="handleOpenEditingModal()">
        {{ $t("core.common.buttons.new") }}
      </VButton>
    </template>
  </VCard>
</template>
