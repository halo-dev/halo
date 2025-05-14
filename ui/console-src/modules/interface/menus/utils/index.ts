import type { MenuItem } from "@halo-dev/api-client";
import { cloneDeep } from "lodash-es";

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
  const parentMap: Record<string, string> = {};

  menuItemsToUpdate.forEach((menuItem) => {
    menuItemsMap[menuItem.metadata.name] = {
      ...menuItem,
      children: [],
    };
    (menuItem.spec.children as string[]).forEach((child) => {
      parentMap[child] = menuItem.metadata.name;
    });
  });

  Object.values(menuItemsMap).forEach((menuTreeItem) => {
    const parentName = parentMap[menuTreeItem.metadata.name];
    if (parentName && menuItemsMap[parentName]) {
      menuItemsMap[parentName].children.push(menuTreeItem);
    }
  });

  const menuTreeItems = Object.values(menuItemsMap).filter(
    (node) => parentMap[node.metadata.name] === undefined
  );

  return sortMenuItemsTree(menuTreeItems);
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
export function convertTreeToMenuItems(menuTreeItems: MenuTreeItem[]) {
  const menuItems: MenuItem[] = [];
  const menuItemsMap = new Map<string, MenuItem>();
  const convertMenuItem = (node: MenuTreeItem | undefined) => {
    if (!node) {
      return;
    }
    const children = node.children || [];
    const { ...rest } = node;
    menuItemsMap.set(node.metadata.name, {
      ...rest,
      spec: {
        ...node.spec,
        children: children.map((child) => child.metadata.name),
      },
    });
    children.forEach((child) => {
      convertMenuItem(child);
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
  const childNames = (menuTreeItem.children || []).map(
    (child) => child.metadata.name
  );
  const { ...rest } = menuTreeItem;
  return {
    ...rest,
    spec: {
      ...menuTreeItem.spec,
      children: childNames,
    },
  };
}
