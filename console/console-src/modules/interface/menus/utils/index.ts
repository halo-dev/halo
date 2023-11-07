import type { MenuItem, MenuItemSpec } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";

export interface MenuTreeItemSpec extends Omit<MenuItemSpec, "children"> {
  children: MenuTreeItem[];
}

export interface MenuTreeItem extends Omit<MenuItem, "spec"> {
  spec: MenuTreeItemSpec;
}

/**
 * Convert a flat array of menu items into flattens a menu tree.
 *
 * Example:
 *
 * ```json
 * [
 *   {
 *     "spec": {
 *       "displayName": "文章分类",
 *       "href": "https://ryanc.cc/categories",
 *       "children": [
 *         "caeef383-3828-4039-9114-6f9ad3b4a37e",
 *         "ded1943d-9fdb-4563-83ee-2f04364872e0"
 *       ]
 *     },
 *     "apiVersion": "v1alpha1",
 *     "kind": "MenuItem",
 *     "metadata": {
 *       "name": "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
 *       "version": 12,
 *       "creationTimestamp": "2022-08-05T04:19:37.252228Z"
 *     }
 *   },
 *   {
 *     "spec": {
 *       "displayName": "Halo",
 *       "href": "https://ryanc.cc/categories/halo",
 *       "children": []
 *     },
 *     "apiVersion": "v1alpha1",
 *     "kind": "MenuItem",
 *     "metadata": {
 *       "name": "caeef383-3828-4039-9114-6f9ad3b4a37e",
 *       "version": 4,
 *       "creationTimestamp": "2022-07-28T06:50:32.777556Z"
 *     }
 *   },
 *   {
 *     "spec": {
 *       "displayName": "Java",
 *       "href": "https://ryanc.cc/categories/java",
 *       "children": []
 *     },
 *     "apiVersion": "v1alpha1",
 *     "kind": "MenuItem",
 *     "metadata": {
 *       "name": "ded1943d-9fdb-4563-83ee-2f04364872e0",
 *       "version": 1,
 *       "creationTimestamp": "2022-08-05T04:22:03.377364Z"
 *     }
 *   }
 * ]
 * ```
 *
 * will be transformed to:
 *
 * ```json
 * [
 *   {
 *     "spec": {
 *       "displayName": "文章分类",
 *       "href": "https://ryanc.cc/categories",
 *       "children": [
 *         {
 *           "spec": {
 *             "displayName": "Halo",
 *             "href": "https://ryanc.cc/categories/halo",
 *             "children": []
 *           },
 *           "apiVersion": "v1alpha1",
 *           "kind": "MenuItem",
 *           "metadata": {
 *             "name": "caeef383-3828-4039-9114-6f9ad3b4a37e",
 *             "version": 4,
 *             "creationTimestamp": "2022-07-28T06:50:32.777556Z"
 *           }
 *         },
 *         {
 *           "spec": {
 *             "displayName": "Java",
 *             "href": "https://ryanc.cc/categories/java",
 *             "children": []
 *           },
 *           "apiVersion": "v1alpha1",
 *           "kind": "MenuItem",
 *           "metadata": {
 *             "name": "ded1943d-9fdb-4563-83ee-2f04364872e0",
 *             "version": 1,
 *             "creationTimestamp": "2022-08-05T04:22:03.377364Z"
 *           }
 *         }
 *       ]
 *     },
 *     "apiVersion": "v1alpha1",
 *     "kind": "MenuItem",
 *     "metadata": {
 *       "name": "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
 *       "version": 12,
 *       "creationTimestamp": "2022-08-05T04:19:37.252228Z"
 *     }
 *   }
 * ]
 * ```
 *
 * @param menuItems
 */
export function buildMenuItemsTree(menuItems: MenuItem[]): MenuTreeItem[] {
  const menuItemsToUpdate = cloneDeep(menuItems);

  const menuItemsMap = {};
  const parentMap = {};

  menuItemsToUpdate.forEach((menuItem) => {
    menuItemsMap[menuItem.metadata.name] = menuItem;
    // @ts-ignore
    menuItem.spec.children.forEach((child) => {
      parentMap[child] = menuItem.metadata.name;
    });
    menuItem.spec.children = [];
  });

  menuItemsToUpdate.forEach((menuItem) => {
    const parentName = parentMap[menuItem.metadata.name];
    if (parentName && menuItemsMap[parentName]) {
      menuItemsMap[parentName].spec.children.push(menuItem);
    }
  });

  const menuTreeItems = menuItemsToUpdate.filter(
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
  menuTreeItems: MenuTreeItem[] | MenuItem[]
): MenuTreeItem[] {
  return menuTreeItems
    .sort((a, b) => {
      if (a.spec.priority < b.spec.priority) {
        return -1;
      }
      if (a.spec.priority > b.spec.priority) {
        return 1;
      }
      return 0;
    })
    .map((menuTreeItem) => {
      if (menuTreeItem.spec.children.length) {
        return {
          ...menuTreeItem,
          spec: {
            ...menuTreeItem.spec,
            children: sortMenuItemsTree(menuTreeItem.spec.children),
          },
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
    if (menuItems[i].spec.children) {
      resetMenuItemsTreePriority(menuItems[i].spec.children);
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
    const children = node.spec.children || [];
    menuItemsMap.set(node.metadata.name, {
      ...node,
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
    if (menuTreeItem.spec.children) {
      menuTreeItem.spec.children.forEach((child) => {
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
  const childNames = menuTreeItem.spec.children.map(
    (child) => child.metadata.name
  );
  return {
    ...menuTreeItem,
    spec: {
      ...menuTreeItem.spec,
      children: childNames,
    },
  };
}
