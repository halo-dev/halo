<script lang="ts" setup>
import {
  useDialog,
  VButton,
  VCard,
  VEmpty,
  VSpace,
  VStatusDot,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import MenuEditingModal from "./MenuEditingModal.vue";
import { defineExpose, onMounted, ref } from "vue";
import type { Menu } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";
import { useRouteQuery } from "@vueuse/router";

const props = withDefaults(
  defineProps<{
    selectedMenu: Menu | null;
  }>(),
  {
    selectedMenu: null,
  }
);

const emit = defineEmits<{
  (event: "select", menu: Menu): void;
  (event: "update:selectedMenu", menu: Menu): void;
}>();

const menus = ref<Menu[]>([] as Menu[]);
const loading = ref(false);
const selectedMenuToUpdate = ref<Menu | null>(null);
const menuEditingModal = ref<boolean>(false);

const dialog = useDialog();

const handleFetchMenus = async () => {
  selectedMenuToUpdate.value = null;
  try {
    loading.value = true;

    const { data } = await apiClient.extension.menu.listv1alpha1Menu();
    menus.value = data.items;

    // update selected menu
    if (props.selectedMenu) {
      const updatedMenu = menus.value.find(
        (menu) => menu.metadata.name === props.selectedMenu?.metadata.name
      );
      if (updatedMenu) {
        emit("update:selectedMenu", updatedMenu);
      }
    }
  } catch (e) {
    console.error("Failed to fetch menus", e);
  } finally {
    loading.value = false;
  }
};

const menuQuery = useRouteQuery("menu");
const handleSelect = (menu: Menu) => {
  emit("update:selectedMenu", menu);
  emit("select", menu);
  menuQuery.value = menu.metadata.name;
};

const handleDeleteMenu = async (menu: Menu) => {
  dialog.warning({
    title: "确定要删除该菜单吗？",
    description: "将同时删除该菜单下的所有菜单项，该操作不可恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await apiClient.extension.menu.deletev1alpha1Menu({
          name: menu.metadata.name,
        });

        const deleteItemsPromises = Array.from(menu.spec.menuItems || []).map(
          (item) =>
            apiClient.extension.menuItem.deletev1alpha1MenuItem({
              name: item,
            })
        );

        await Promise.all(deleteItemsPromises);
      } catch (e) {
        console.error("Failed to delete menu", e);
      } finally {
        await handleFetchMenus();
      }
    },
  });
};

const handleOpenEditingModal = (menu: Menu | null) => {
  selectedMenuToUpdate.value = menu;
  menuEditingModal.value = true;
};

onMounted(async () => {
  await handleFetchMenus();

  if (menuQuery.value) {
    const menu = menus.value.find((m) => m.metadata.name === menuQuery.value);
    if (menu) {
      handleSelect(menu);
    }
    return;
  }

  if (menus.value.length > 0) {
    handleSelect(menus.value[0]);
  }
});

defineExpose({
  handleFetchMenus,
});
</script>
<template>
  <MenuEditingModal
    v-model:visible="menuEditingModal"
    :menu="selectedMenuToUpdate"
    @close="handleFetchMenus"
    @created="handleSelect"
  />
  <VCard :body-class="['!p-0']" title="菜单">
    <VEmpty
      v-if="!menus.length && !loading"
      message="你可以尝试刷新或者新建菜单"
      title="当前没有菜单"
    >
      <template #actions>
        <VSpace>
          <VButton size="sm" @click="handleFetchMenus"> 刷新</VButton>
        </VSpace>
      </template>
    </VEmpty>
    <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
      <li
        v-for="(menu, index) in menus"
        :key="index"
        @click="handleSelect(menu)"
      >
        <VEntity
          :is-selected="selectedMenu?.metadata.name === menu.metadata.name"
        >
          <template #start>
            <VEntityField
              :title="menu.spec?.displayName"
              :description="`${menu.spec.menuItems?.length || 0} 个菜单项`"
            ></VEntityField>
          </template>
          <template #end>
            <VEntityField v-if="menu.metadata.deletionTimestamp">
              <template #description>
                <VStatusDot v-tooltip="`删除中`" state="warning" animate />
              </template>
            </VEntityField>
          </template>
          <template #dropdownItems>
            <VButton
              v-close-popper
              block
              type="secondary"
              @click="handleOpenEditingModal(menu)"
            >
              修改
            </VButton>
            <VButton
              v-close-popper
              block
              type="danger"
              @click="handleDeleteMenu(menu)"
            >
              删除
            </VButton>
          </template>
        </VEntity>
      </li>
    </ul>
    <template #footer>
      <VButton block type="secondary" @click="handleOpenEditingModal(null)">
        新增
      </VButton>
    </template>
  </VCard>
</template>
