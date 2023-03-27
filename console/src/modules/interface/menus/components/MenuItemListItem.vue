<script lang="ts" setup>
import {
  IconList,
  VTag,
  VStatusDot,
  VEntity,
  VEntityField,
  VDropdownItem,
} from "@halo-dev/components";
import Draggable from "vuedraggable";
import { ref } from "vue";
import type { MenuTreeItem } from "@/modules/interface/menus/utils";
import { usePermission } from "@/utils/permission";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

withDefaults(
  defineProps<{
    menuTreeItems: MenuTreeItem[];
  }>(),
  {
    menuTreeItems: () => [],
  }
);

const emit = defineEmits<{
  (event: "change"): void;
  (event: "open-editing", menuItem: MenuTreeItem): void;
  (event: "open-create-by-parent", menuItem: MenuTreeItem): void;
  (event: "delete", menuItem: MenuTreeItem): void;
}>();

const isDragging = ref(false);

function onChange() {
  emit("change");
}

function onOpenEditingModal(menuItem: MenuTreeItem) {
  emit("open-editing", menuItem);
}

function onOpenCreateByParentModal(menuItem: MenuTreeItem) {
  emit("open-create-by-parent", menuItem);
}

function onDelete(menuItem: MenuTreeItem) {
  emit("delete", menuItem);
}

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
  <draggable
    :list="menuTreeItems"
    class="box-border h-full w-full divide-y divide-gray-100"
    ghost-class="opacity-50"
    group="menu-item"
    handle=".drag-element"
    item-key="metadata.name"
    tag="ul"
    @change="onChange"
    @end="isDragging = false"
    @start="isDragging = true"
  >
    <template #item="{ element: menuItem }">
      <li>
        <VEntity>
          <template #prepend>
            <div
              v-permission="['system:menus:manage']"
              class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
            >
              <IconList class="h-3.5 w-3.5" />
            </div>
          </template>
          <template #start>
            <VEntityField :title="menuItem.status?.displayName">
              <template #extra>
                <VTag v-if="getMenuItemRefDisplayName(menuItem)">
                  {{ getMenuItemRefDisplayName(menuItem) }}
                </VTag>
              </template>
              <template #description>
                <a
                  v-if="menuItem.status?.href"
                  :href="menuItem.status?.href"
                  :title="menuItem.status?.href"
                  target="_blank"
                  class="truncate text-xs text-gray-500 group-hover:text-gray-900"
                >
                  {{ menuItem.status.href }}
                </a>
              </template>
            </VEntityField>
          </template>
          <template #end>
            <VEntityField v-if="menuItem.metadata.deletionTimestamp">
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
            <VDropdownItem @click="onOpenEditingModal(menuItem)">
              {{ $t("core.common.buttons.edit") }}
            </VDropdownItem>
            <VDropdownItem @click="onOpenCreateByParentModal(menuItem)">
              {{ $t("core.menu.operations.add_sub_menu_item.button") }}
            </VDropdownItem>
            <VDropdownItem type="danger" @click="onDelete(menuItem)">
              {{ $t("core.common.buttons.delete") }}
            </VDropdownItem>
          </template>
        </VEntity>
        <MenuItemListItem
          :menu-tree-items="menuItem.spec.children"
          class="pl-10 transition-all duration-300"
          @change="onChange"
          @delete="onDelete"
          @open-editing="onOpenEditingModal"
          @open-create-by-parent="onOpenCreateByParentModal"
        />
      </li>
    </template>
  </draggable>
</template>
