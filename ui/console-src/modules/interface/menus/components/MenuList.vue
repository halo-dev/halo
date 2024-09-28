<script lang="ts" setup>
import { usePermission } from "@/utils/permission";
import type { Menu } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VButton,
  VCard,
  VDropdownItem,
  VEmpty,
  VEntity,
  VEntityField,
  VLoading,
  VStatusDot,
  VTag,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import MenuEditingModal from "./MenuEditingModal.vue";

interface SystemMenuConfig {
  primary: string;
}

const { currentUserHasPermission } = usePermission();
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
    const { data } = await coreApiClient.menu.listMenu({
      page: 0,
      size: 0,
    });
    return data.items;
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

const menuQuery = useRouteQuery("menu");
const handleSelect = (menu: Menu) => {
  emit("update:selectedMenu", menu);
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
        await coreApiClient.menu.deleteMenu({
          name: menu.metadata.name,
        });

        const deleteItemsPromises = menu.spec.menuItems?.map((item) =>
          coreApiClient.menuItem.deleteMenuItem({
            name: item,
          })
        );

        if (!deleteItemsPromises) {
          return;
        }

        await Promise.all(deleteItemsPromises);

        Toast.success(t("core.common.toast.delete_success"));
      } catch (e) {
        console.error("Failed to delete menu", e);
      } finally {
        await refetch();
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
              <VDropdownItem
                v-if="primaryMenuName !== menu.metadata.name"
                @click="handleSetPrimaryMenu(menu)"
              >
                {{ $t("core.menu.operations.set_primary.button") }}
              </VDropdownItem>
              <VDropdownItem @click="handleOpenEditingModal(menu)">
                {{ $t("core.common.buttons.edit") }}
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
