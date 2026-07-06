<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import type { MenuItem, MenuItemTreeNode } from "@halo-dev/api-client";
import { IconArrowRight, VDropdown } from "@halo-dev/components";
import { onClickOutside, useResizeObserver } from "@vueuse/core";
import { computed, shallowRef, useTemplateRef } from "vue";
import { filterMenuItemTreeNodes } from "../utils";
import MenuItemParentSelectNode from "./MenuItemParentSelectNode.vue";

const props = defineProps<{
  context: FormKitFrameworkContext;
}>();

const dropdownVisible = shallowRef(false);
const wrapperRef = useTemplateRef<HTMLElement>("wrapperRef");
const popperRef = useTemplateRef<HTMLElement>("popperRef");

useResizeObserver(wrapperRef, () => {
  window.dispatchEvent(new Event("resize"));
});

onClickOutside(
  wrapperRef,
  () => {
    dropdownVisible.value = false;
  },
  {
    ignore: [popperRef],
  }
);

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
  <VDropdown
    :triggers="[]"
    :shown="dropdownVisible"
    auto-size
    :auto-hide="false"
    container="body"
    :distance="10"
    class="w-full"
    popper-class="menu-item-parent-select-dropdown"
  >
    <div ref="wrapperRef" class="flex w-full items-center">
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
        @click="toggleDropdown"
        @keydown.delete="handleDelete"
        @keydown.enter.prevent="toggleDropdown"
        @keydown.space.prevent="toggleDropdown"
      />
      <div
        class="inline-flex h-full cursor-pointer items-center px-1"
        @click="toggleDropdown"
      >
        <IconArrowRight class="rotate-90 text-gray-500 hover:text-gray-700" />
      </div>
    </div>

    <template #popper>
      <div ref="popperRef" class="max-h-96 w-full overflow-auto bg-white">
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
  </VDropdown>
</template>

<style lang="scss">
.menu-item-parent-select-dropdown {
  .v-popper__arrow-container {
    display: none;
  }
  .v-popper__inner {
    padding: 0 !important;
  }
}
</style>
