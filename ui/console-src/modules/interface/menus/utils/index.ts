import type { JsonPatchInner, MenuItem } from "@halo-dev/api-client";
import { cloneDeep } from "es-toolkit";

export interface MenuTreeItem extends MenuItem {
  children: MenuTreeItem[];
}

/**
 * Convert a flat array of menu items into a menu tree with children at the top level.
 *
 * @param menuItems
 */
export function buildMenuItemsTree(menuItems: MenuItem[]): MenuTreeItem[] {
  const menuItemsToUpdate = cloneDeep(menuItems);

  const menuItemsMap: Record<string, MenuTreeItem> = {};

  menuItemsToUpdate.forEach((menuItem) => {
    menuItemsMap[menuItem.metadata.name] = {
      ...menuItem,
      children: [],
    };
  });

  Object.values(menuItemsMap).forEach((menuTreeItem) => {
    const parentName = menuTreeItem.spec.parent;
    if (hasValidParent(menuTreeItem, menuItemsMap)) {
      menuItemsMap[parentName as string].children.push(menuTreeItem);
    }
  });

  const menuTreeItems = Object.values(menuItemsMap).filter(
    (node) => !hasValidParent(node, menuItemsMap)
  );

  return sortMenuItemsTree(menuTreeItems);
}

function hasValidParent(
  menuTreeItem: MenuTreeItem,
  menuItemsMap: Record<string, MenuTreeItem>
) {
  const parentName = menuTreeItem.spec.parent;
  if (!parentName || parentName === menuTreeItem.metadata.name) {
    return false;
  }
  if (!menuItemsMap[parentName]) {
    return false;
  }

  let ancestorName: string | undefined = parentName;
  while (ancestorName) {
    if (ancestorName === menuTreeItem.metadata.name) {
      return false;
    }
    ancestorName = menuItemsMap[ancestorName]?.spec.parent;
  }
  return true;
}

/**
 * Sort a menu tree by priority.
 *
 * @param menuTreeItems
 */
export function sortMenuItemsTree(
  menuTreeItems: MenuTreeItem[]
): MenuTreeItem[] {
  return menuTreeItems
    .sort((a, b) => {
      const aPriority = a.spec.priority ?? 0;
      const bPriority = b.spec.priority ?? 0;
      if (aPriority < bPriority) {
        return -1;
      }
      if (aPriority > bPriority) {
        return 1;
      }
      return 0;
    })
    .map((menuTreeItem) => {
      if (menuTreeItem.children.length) {
        return {
          ...menuTreeItem,
          children: sortMenuItemsTree(menuTreeItem.children),
        };
      }
      return menuTreeItem;
    });
}

/**
 * Reset the menu tree item's priority.
 *
 * @param menuItems
 */
export function resetMenuItemsTreePriority(
  menuItems: MenuTreeItem[]
): MenuTreeItem[] {
  for (let i = 0; i < menuItems.length; i++) {
    menuItems[i].spec.priority = i;
    if (menuItems[i].children) {
      resetMenuItemsTreePriority(menuItems[i].children);
    }
  }
  return menuItems;
}

/**
 * Convert a menu tree items into a flat array of menu.
 *
 * @param menuTreeItems
 */
export function convertTreeToMenuItems(
  menuTreeItems: MenuTreeItem[],
  menuName?: string
) {
  const menuItems: MenuItem[] = [];
  const menuItemsMap = new Map<string, MenuItem>();
  const convertMenuItem = (
    node: MenuTreeItem | undefined,
    parentName?: string
  ) => {
    if (!node) {
      return;
    }
    const children = node.children || [];
    const { children: _, ...rest } = node;
    menuItemsMap.set(node.metadata.name, {
      ...rest,
      spec: {
        ...node.spec,
        menuName: menuName || node.spec.menuName,
        parent: parentName,
      },
    });
    children.forEach((child) => {
      convertMenuItem(child, node.metadata.name);
    });
  };
  menuTreeItems.forEach((node) => {
    convertMenuItem(node);
  });
  menuItemsMap.forEach((node) => {
    menuItems.push(node);
  });
  return menuItems;
}

export function getChildrenNames(menuTreeItem: MenuTreeItem): string[] {
  const childrenNames: string[] = [];

  function getChildrenNamesRecursive(menuTreeItem: MenuTreeItem) {
    if (menuTreeItem.children) {
      menuTreeItem.children.forEach((child) => {
        childrenNames.push(child.metadata.name);
        getChildrenNamesRecursive(child);
      });
    }
  }

  getChildrenNamesRecursive(menuTreeItem);

  return childrenNames;
}

/**
 * Convert {@link MenuTreeItem} to {@link MenuItem} with flat children name array.
 *
 * @param menuTreeItem
 */
export function convertMenuTreeItemToMenuItem(
  menuTreeItem: MenuTreeItem
): MenuItem {
  const { children: _, ...rest } = menuTreeItem;
  return {
    ...rest,
    spec: {
      ...menuTreeItem.spec,
    },
  };
}

export function resolveClonedParentName(
  menuItem: MenuItem,
  oldToNewNameMap: Map<string, string>
) {
  const parentName = menuItem.spec.parent;
  if (!parentName) {
    return undefined;
  }
  return oldToNewNameMap.get(parentName);
}

export function buildMenuItemHierarchyPatch(
  menuItem: MenuItem,
  menuName: string,
  previousParentName?: string
): JsonPatchInner[] {
  const patch: JsonPatchInner[] = [
    {
      op: "add",
      path: "/spec/priority",
      value: menuItem.spec.priority ?? 0,
    },
    {
      op: "add",
      path: "/spec/menuName",
      value: menuName,
    },
  ];

  if (menuItem.spec.parent) {
    patch.push({
      op: "add",
      path: "/spec/parent",
      value: menuItem.spec.parent,
    });
  } else if (previousParentName) {
    patch.push({
      op: "remove",
      path: "/spec/parent",
    });
  }

  return patch;
}
