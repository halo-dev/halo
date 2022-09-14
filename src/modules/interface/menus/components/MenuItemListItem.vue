<script lang="ts" setup>
import {
  IconList,
  VButton,
  VTag,
  VStatusDot,
  VEntity,
  VEntityField,
} from "@halo-dev/components";
import Draggable from "vuedraggable";
import { ref } from "vue";
import type { MenuTreeItem } from "@/modules/interface/menus/utils";

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
  (event: "delete", menuItem: MenuTreeItem): void;
}>();

const isDragging = ref(false);

function onChange() {
  emit("change");
}

function onOpenEditingModal(menuItem: MenuTreeItem) {
  emit("open-editing", menuItem);
}

function onDelete(menuItem: MenuTreeItem) {
  emit("delete", menuItem);
}

function getMenuItemRefDisplayName(menuItem: MenuTreeItem) {
  if (menuItem.spec.postRef) {
    return "文章";
  }
  if (menuItem.spec.singlePageRef) {
    return "自定义页面";
  }
  if (menuItem.spec.categoryRef) {
    return "分类";
  }
  if (menuItem.spec.tagRef) {
    return "标签";
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
              class="drag-element absolute inset-y-0 left-0 hidden w-3.5 cursor-move items-center bg-gray-100 transition-all hover:bg-gray-200 group-hover:flex"
            >
              <IconList class="h-3.5 w-3.5" />
            </div>
          </template>
          <template #start>
            <VEntityField
              :title="menuItem.status.displayName"
              :description="menuItem.status.href"
            >
              <template #extra>
                <VTag v-if="getMenuItemRefDisplayName(menuItem)">
                  {{ getMenuItemRefDisplayName(menuItem) }}
                </VTag>
              </template>
            </VEntityField>
          </template>
          <template #end>
            <VEntityField v-if="menuItem.metadata.deletionTimestamp">
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
              @click="onOpenEditingModal(menuItem)"
            >
              修改
            </VButton>
            <VButton
              v-close-popper
              block
              type="danger"
              @click="onDelete(menuItem)"
            >
              删除
            </VButton>
          </template>
        </VEntity>
        <MenuItemListItem
          :menu-tree-items="menuItem.spec.children"
          class="pl-10 transition-all duration-300"
          @change="onChange"
          @delete="onDelete"
          @open-editing="onOpenEditingModal"
        />
      </li>
    </template>
  </draggable>
</template>
