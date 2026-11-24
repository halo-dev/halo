<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import type { MenuItem, MenuItemTreeNode } from "@halo-dev/api-client";
import { IconArrowRight, VPopover } from "@halo-dev/components";
import { computed, shallowRef } from "vue";
import { filterMenuItemTreeNodes } from "../utils";
import MenuItemParentSelectNode from "./MenuItemParentSelectNode.vue";

const props = defineProps<{
  context: FormKitFrameworkContext;
}>();

const dropdownVisible = shallowRef(false);

const menuItemTree = computed<MenuItemTreeNode[]>(() => {
  const tree = props.context.node.props.menuItemTree;
  return Array.isArray(tree) ? (tree as MenuItemTreeNode[]) : [];
});

const excludedNames = computed<string[]>(() => {
  const names = props.context.node.props.excludedNames;
  if (Array.isArray(names)) {
    return names.filter((name): name is string => typeof name === "string");
  }
  if (typeof names === "string") {
    return names.split(",").map((name) => name.trim());
  }
  return [];
});

const filteredMenuItemTree = computed(() => {
  return filterMenuItemTreeNodes(menuItemTree.value, excludedNames.value);
});

const selectedName = computed(() => {
  return typeof props.context._value === "string" ? props.context._value : "";
});

const disabled = computed(() => {
  return props.context.node.props.disabled === true;
});

const selectedLabel = computed(() => {
  if (!selectedName.value) {
    return "";
  }

  const path = getMenuItemPath(filteredMenuItemTree.value, selectedName.value);
  if (!path) {
    return selectedName.value;
  }

  return path.map((node) => getMenuItemLabel(node.menuItem)).join(" / ");
});

const excludedNameSet = computed(() => {
  return new Set(excludedNames.value.filter(Boolean));
});

function handleSelect(name: string) {
  if (name && excludedNameSet.value.has(name)) {
    return;
  }
  props.context.node.input(selectedName.value === name ? "" : name);
  dropdownVisible.value = false;
}

function toggleDropdown() {
  if (disabled.value) {
    return;
  }
  dropdownVisible.value = !dropdownVisible.value;
}

function handleDelete() {
  if (selectedName.value) {
    props.context.node.input("");
  }
}

function getMenuItemPath(
  nodes: MenuItemTreeNode[],
  name: string,
  parents: MenuItemTreeNode[] = []
): MenuItemTreeNode[] | undefined {
  for (const node of nodes) {
    const path = [...parents, node];
    if (node.menuItem.metadata.name === name) {
      return path;
    }
    const childPath = getMenuItemPath(node.children, name, path);
    if (childPath) {
      return childPath;
    }
  }
}

function getMenuItemLabel(menuItem: MenuItem) {
  return (
    menuItem.status?.displayName ||
    menuItem.spec.displayName ||
    menuItem.metadata.name
  );
}
</script>

<template>
  <VPopover
    v-model:open="dropdownVisible"
    :disabled="disabled"
    :arrow="false"
    :offset="10"
    match-trigger-width
    content-class="!p-0"
  >
    <div
      class="flex w-full items-center"
      :class="{ 'pointer-events-none': disabled }"
    >
      <input
        :id="context.id"
        :value="selectedLabel"
        :disabled="disabled"
        type="text"
        readonly
        aria-haspopup="listbox"
        :aria-expanded="dropdownVisible"
        class="block w-0 flex-grow cursor-pointer bg-transparent px-3 py-1 text-sm text-black caret-transparent transition-all focus:outline-none focus:ring-0"
        @blur="context.handlers.blur()"
        @keydown.delete="handleDelete"
        @keydown.enter.prevent="toggleDropdown"
        @keydown.space.prevent="toggleDropdown"
      />
      <div class="inline-flex h-full cursor-pointer items-center px-1">
        <IconArrowRight class="rotate-90 text-gray-500 hover:text-gray-700" />
      </div>
    </div>

    <template #content>
      <div class="max-h-96 w-full overflow-auto">
        <ul class="p-1" role="listbox">
          <MenuItemParentSelectNode
            v-for="node in filteredMenuItemTree"
            :key="node.menuItem.metadata.name"
            :node="node"
            :selected-name="selectedName"
            @select="handleSelect"
          />
        </ul>
      </div>
    </template>
  </VPopover>
</template>
