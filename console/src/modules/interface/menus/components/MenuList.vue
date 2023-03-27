<script lang="ts" setup>
import {
  Dialog,
  VButton,
  VCard,
  VEmpty,
  VSpace,
  VStatusDot,
  VEntity,
  VEntityField,
  VTag,
  VLoading,
  Toast,
  VDropdownItem,
} from "@halo-dev/components";
import MenuEditingModal from "./MenuEditingModal.vue";
import { onMounted, onUnmounted, ref } from "vue";
import type { Menu } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { usePermission } from "@/utils/permission";
import { onBeforeRouteLeave } from "vue-router";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

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
const selectedMenuToUpdate = ref<Menu>();
const menuEditingModal = ref<boolean>(false);
const refreshInterval = ref();

const handleFetchMenus = async (options?: { mute?: boolean }) => {
  selectedMenuToUpdate.value = undefined;
  try {
    clearInterval(refreshInterval.value);

    if (!options?.mute) {
      loading.value = true;
    }

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

    const deletedMenus = menus.value.filter(
      (menu) => !!menu.metadata.deletionTimestamp
    );

    if (deletedMenus.length) {
      refreshInterval.value = setInterval(() => {
        handleFetchMenus({ mute: true });
      }, 3000);
    }
  } catch (e) {
    console.error("Failed to fetch menus", e);
  } finally {
    loading.value = false;
  }
};

onUnmounted(() => {
  clearInterval(refreshInterval.value);
});

onBeforeRouteLeave(() => {
  clearInterval(refreshInterval.value);
});

const menuQuery = useRouteQuery("menu");
const handleSelect = (menu: Menu) => {
  emit("update:selectedMenu", menu);
  emit("select", menu);
  menuQuery.value = menu.metadata.name;
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

        Toast.success(t("core.common.toast.delete_success"));
      } catch (e) {
        console.error("Failed to delete menu", e);
      } finally {
        await handleFetchMenus();
      }
    },
  });
};

const handleOpenEditingModal = (menu?: Menu) => {
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

// primary menu
const primaryMenuName = ref<string>();

const handleFetchPrimaryMenuName = async () => {
  const { data } = await apiClient.extension.configMap.getv1alpha1ConfigMap({
    name: "system",
  });

  if (!data.data?.menu) {
    return;
  }

  const menuConfig = JSON.parse(data.data.menu);

  primaryMenuName.value = menuConfig.primary;
};

const handleSetPrimaryMenu = async (menu: Menu) => {
  const { data: systemConfigMap } =
    await apiClient.extension.configMap.getv1alpha1ConfigMap({
      name: "system",
    });

  if (systemConfigMap.data) {
    const menuConfigToUpdate = JSON.parse(systemConfigMap.data?.menu || "{}");
    menuConfigToUpdate.primary = menu.metadata.name;
    systemConfigMap.data["menu"] = JSON.stringify(menuConfigToUpdate);

    await apiClient.extension.configMap.updatev1alpha1ConfigMap({
      name: "system",
      configMap: systemConfigMap,
    });
  }
  await handleFetchPrimaryMenuName();

  Toast.success(t("core.menu.operations.set_primary.toast_success"));
};

onMounted(handleFetchPrimaryMenuName);
</script>
<template>
  <MenuEditingModal
    v-model:visible="menuEditingModal"
    :menu="selectedMenuToUpdate"
    @close="handleFetchMenus()"
    @created="handleSelect"
  />
  <VCard :body-class="['!p-0']" :title="$t('core.menu.title')">
    <VLoading v-if="loading" />
    <Transition v-else-if="!menus.length" appear name="fade">
      <VEmpty
        :message="$t('core.menu.empty.message')"
        :title="$t('core.menu.empty.title')"
      >
        <template #actions>
          <VSpace>
            <VButton size="sm" @click="handleFetchMenus()">
              {{ $t("core.common.buttons.refresh") }}
            </VButton>
          </VSpace>
        </template>
      </VEmpty>
    </Transition>
    <Transition v-else appear name="fade">
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
                :description="
                  $t('core.menu.list.fields.items_count', {
                    count: menu.spec.menuItems?.length || 0,
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
              v-if="currentUserHasPermission(['system:menus:manage'])"
              #dropdownItems
            >
              <VDropdownItem @click="handleSetPrimaryMenu(menu)">
                {{ $t("core.menu.operations.set_primary.button") }}
              </VDropdownItem>
              <VDropdownItem @click="handleOpenEditingModal(menu)">
                {{ $t("core.common.buttons.edit") }}
              </VDropdownItem>
              <VDropdownItem type="danger" @click="handleDeleteMenu(menu)">
                {{ $t("core.common.buttons.delete") }}
              </VDropdownItem>
            </template>
          </VEntity>
        </li>
      </ul>
    </Transition>
    <template v-if="currentUserHasPermission(['system:menus:manage'])" #footer>
      <VButton block type="secondary" @click="handleOpenEditingModal()">
        {{ $t("core.common.buttons.new") }}
      </VButton>
    </template>
  </VCard>
</template>
