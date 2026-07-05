<script lang="ts" setup>
import type { MenuItem, MenuItemTreeNode } from "@halo-dev/api-client";
import { IconCheckboxCircle } from "@halo-dev/components";
import { computed } from "vue";

const props = defineProps<{
  node: MenuItemTreeNode;
  selectedName?: string;
}>();

const emit = defineEmits<{
  (event: "select", name: string): void;
}>();

const menuItem = computed(() => props.node.menuItem);

function getMenuItemLabel(menuItem: MenuItem) {
  return (
    menuItem.status?.displayName ||
    menuItem.spec.displayName ||
    menuItem.metadata.name
  );
}
</script>

<template>
  <li :id="`menu-item-parent-${menuItem.metadata.name}`">
    <button
      type="button"
      class="flex w-full cursor-pointer items-center justify-between rounded p-2 text-left hover:bg-gray-100"
      :class="{
        'bg-gray-100': selectedName === menuItem.metadata.name,
      }"
      @click="emit('select', menuItem.metadata.name)"
    >
      <span class="flex-1 truncate text-xs text-gray-700">
        {{ getMenuItemLabel(menuItem) }}
      </span>
      <IconCheckboxCircle
        class="h-5 w-5 text-primary opacity-0"
        :class="{ 'opacity-100': selectedName === menuItem.metadata.name }"
      />
    </button>

    <ul v-if="node.children.length > 0" class="my-2.5 ml-2.5 border-l pl-1.5">
      <MenuItemParentSelectNode
        v-for="child in node.children"
        :key="child.menuItem.metadata.name"
        :node="child"
        :selected-name="selectedName"
        @select="emit('select', $event)"
      />
    </ul>
  </li>
</template>
