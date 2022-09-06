<script lang="ts" setup>
import {
  IconSettings,
  useDialog,
  VButton,
  VCard,
  VEmpty,
  VSpace,
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
    <div class="divide-y divide-gray-100 bg-white">
      <div
        v-for="(menu, index) in menus"
        :key="index"
        :class="{
          'bg-gray-50': selectedMenu?.metadata.name === menu.metadata.name,
        }"
        class="relative flex items-center p-4"
        @click="handleSelect(menu)"
      >
        <div
          v-if="selectedMenu?.metadata.name === menu.metadata.name"
          class="absolute inset-y-0 left-0 w-0.5 bg-primary"
        ></div>
        <span class="flex flex-1 cursor-pointer flex-col gap-y-1">
          <span class="block text-sm font-medium">
            {{ menu.spec?.displayName }}
          </span>
          <span class="block text-xs text-gray-400">
            {{ menu.spec.menuItems?.length || 0 }}
            个菜单项
          </span>
        </span>
        <FloatingTooltip
          v-if="menu.metadata.deletionTimestamp"
          class="mr-4 hidden items-center sm:flex"
        >
          <div class="inline-flex h-1.5 w-1.5 rounded-full bg-red-600">
            <span
              class="inline-block h-1.5 w-1.5 animate-ping rounded-full bg-red-600"
            ></span>
          </div>
          <template #popper> 删除中</template>
        </FloatingTooltip>
        <div class="self-center">
          <FloatingDropdown>
            <IconSettings
              class="cursor-pointer transition-all hover:text-blue-600"
            />
            <template #popper>
              <div class="w-48 p-2">
                <VSpace class="w-full" direction="column">
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
                </VSpace>
              </div>
            </template>
          </FloatingDropdown>
        </div>
      </div>
    </div>
    <template #footer>
      <VButton block type="secondary" @click="handleOpenEditingModal(null)">
        新增
      </VButton>
    </template>
  </VCard>
</template>
