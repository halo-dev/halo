import type {
  JsonPatchInner,
  Menu,
  MenuItem,
  MenuItemTreeNode,
} from "@halo-dev/api-client";

export interface MenuItemMovePosition {
  name: string;
  parentName?: string;
  beforeName?: string;
}

export function findFallbackMenuAfterDelete(
  menus: Menu[],
  deletedName: string
) {
  return menus.find(
    (menu) =>
      menu.metadata.name !== deletedName && !menu.metadata.deletionTimestamp
  );
}

interface TreeNodePosition {
  parentName?: string;
  beforeName?: string;
}

export function flattenMenuItemTreeNodes(
  tree: MenuItemTreeNode[]
): MenuItemTreeNode[] {
  const nodes: MenuItemTreeNode[] = [];

  function collect(current: MenuItemTreeNode[]) {
    current.forEach((node) => {
      nodes.push(node);
      collect(node.children);
    });
  }

  collect(tree);
  return nodes;
}

export function getMenuItemTreeNodeChildrenNames(
  menuItemTreeNode: MenuItemTreeNode
): string[] {
  const childrenNames: string[] = [];

  function collect(node: MenuItemTreeNode) {
    node.children.forEach((child) => {
      childrenNames.push(child.menuItem.metadata.name);
      collect(child);
    });
  }

  collect(menuItemTreeNode);
  return childrenNames;
}

export function getSelectableParentMenuItemTreeNodes(
  tree: MenuItemTreeNode[],
  currentMenuItemName?: string
): MenuItemTreeNode[] {
  return flattenMenuItemTreeNodes(
    filterMenuItemTreeNodes(
      tree,
      currentMenuItemName ? [currentMenuItemName] : []
    )
  );
}

export function filterMenuItemTreeNodes(
  tree: MenuItemTreeNode[],
  excludedNames: string[] = []
): MenuItemTreeNode[] {
  const excludedNameSet = new Set(excludedNames.filter(Boolean));

  function collect(nodes: MenuItemTreeNode[]): MenuItemTreeNode[] {
    return nodes.flatMap((node) => {
      if (excludedNameSet.has(node.menuItem.metadata.name)) {
        return [];
      }

      return [
        {
          ...node,
          children: collect(node.children),
        },
      ];
    });
  }

  return collect(tree);
}

export function buildMenuItemParentMovePosition(
  menuItemName: string,
  previousParentName?: string,
  selectedParentName?: string
): MenuItemMovePosition | undefined {
  const normalizedPreviousParentName = previousParentName || undefined;
  const normalizedSelectedParentName = selectedParentName || undefined;

  if (normalizedPreviousParentName === normalizedSelectedParentName) {
    return undefined;
  }

  return {
    name: menuItemName,
    parentName: normalizedSelectedParentName,
    beforeName: undefined,
  };
}

export function buildMenuItemPositionRequest(
  previousTree: MenuItemTreeNode[],
  currentTree: MenuItemTreeNode[]
): MenuItemMovePosition | undefined {
  const previousPositions = flattenMenuItemTreePositions(previousTree);
  const currentPositions = flattenMenuItemTreePositions(currentTree);
  const candidates = Array.from(currentPositions.entries())
    .filter(([name, position]) => {
      const previousPosition = previousPositions.get(name);
      if (!previousPosition) {
        return false;
      }
      return (
        previousPosition.parentName !== position.parentName ||
        previousPosition.beforeName !== position.beforeName
      );
    })
    .map(([name, position]) => ({ name, ...position }));

  return candidates.find((candidate) =>
    sameTreeAfterMove(previousTree, currentTree, candidate)
  );
}

function flattenMenuItemTreePositions(tree: MenuItemTreeNode[]) {
  const positions = new Map<string, TreeNodePosition>();

  function collect(nodes: MenuItemTreeNode[], parentName?: string) {
    nodes.forEach((node, index) => {
      const name = node.menuItem.metadata.name;
      positions.set(name, {
        parentName,
        beforeName: nodes[index + 1]?.menuItem.metadata.name,
      });
      collect(node.children, name);
    });
  }

  collect(tree);
  return positions;
}

function sameTreeAfterMove(
  previousTree: MenuItemTreeNode[],
  currentTree: MenuItemTreeNode[],
  move: MenuItemMovePosition
) {
  const movedTree = cloneTree(previousTree);
  const movedNode = detachNode(movedTree, move.name);
  if (!movedNode) {
    return false;
  }
  if (!insertNode(movedTree, movedNode, move.parentName, move.beforeName)) {
    return false;
  }
  return sameTreeNames(movedTree, currentTree);
}

function cloneTree(tree: MenuItemTreeNode[]): MenuItemTreeNode[] {
  return tree.map((node) => ({
    menuItem: node.menuItem,
    children: cloneTree(node.children),
  }));
}

function detachNode(
  nodes: MenuItemTreeNode[],
  name: string
): MenuItemTreeNode | undefined {
  const index = nodes.findIndex((node) => node.menuItem.metadata.name === name);
  if (index >= 0) {
    return nodes.splice(index, 1)[0];
  }

  for (const node of nodes) {
    const detached = detachNode(node.children, name);
    if (detached) {
      return detached;
    }
  }
}

function insertNode(
  nodes: MenuItemTreeNode[],
  movedNode: MenuItemTreeNode,
  parentName?: string,
  beforeName?: string
) {
  const siblings = parentName ? findNode(nodes, parentName)?.children : nodes;
  if (!siblings) {
    return false;
  }

  if (!beforeName) {
    siblings.push(movedNode);
    return true;
  }

  const index = siblings.findIndex(
    (node) => node.menuItem.metadata.name === beforeName
  );
  if (index < 0) {
    return false;
  }
  siblings.splice(index, 0, movedNode);
  return true;
}

function findNode(
  nodes: MenuItemTreeNode[],
  name: string
): MenuItemTreeNode | undefined {
  for (const node of nodes) {
    if (node.menuItem.metadata.name === name) {
      return node;
    }
    const child = findNode(node.children, name);
    if (child) {
      return child;
    }
  }
}

function sameTreeNames(left: MenuItemTreeNode[], right: MenuItemTreeNode[]) {
  if (left.length !== right.length) {
    return false;
  }

  return left.every((leftNode, index) => {
    const rightNode = right[index];
    return (
      leftNode.menuItem.metadata.name === rightNode.menuItem.metadata.name &&
      sameTreeNames(leftNode.children, rightNode.children)
    );
  });
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
