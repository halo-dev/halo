import type { Category, CategoryTreeNode } from "@halo-dev/api-client";

export type { CategoryTreeNode };

export interface CategoryMovePosition {
  name: string;
  parentName?: string;
  beforeName?: string;
}

type CategoryOrTreeNode = Category | CategoryTreeNode;

interface TreeNodePosition {
  parentName?: string;
  beforeName?: string;
}

export function getCategoryFromNode(category: CategoryOrTreeNode): Category {
  if ("category" in category) {
    return category.category;
  }
  return category;
}

export function flattenCategoryTreeNodes(tree: CategoryTreeNode[]): Category[] {
  const categories: Category[] = [];

  function collect(nodes: CategoryTreeNode[]) {
    nodes.forEach((node) => {
      categories.push(node.category);
      collect(node.children);
    });
  }

  collect(tree);
  return categories;
}

export function convertCategoryTreeToCategory(
  categoryTree: CategoryTreeNode
): Category {
  return {
    ...categoryTree.category,
    metadata: {
      ...categoryTree.category.metadata,
    },
    spec: {
      ...categoryTree.category.spec,
    },
    status: categoryTree.category.status
      ? {
          ...categoryTree.category.status,
        }
      : undefined,
  };
}

export function getSelectableParentCategoryTreeNodes(
  tree: CategoryTreeNode[],
  currentCategoryName?: string
): CategoryTreeNode[] {
  return flattenTreeNodes(
    filterCategoryTreeNodes(
      tree,
      currentCategoryName ? [currentCategoryName] : []
    )
  );
}

export function filterCategoryTreeNodes(
  tree: CategoryTreeNode[],
  excludedNames: string[] = []
): CategoryTreeNode[] {
  const excludedNameSet = new Set(excludedNames.filter(Boolean));

  function collect(nodes: CategoryTreeNode[]): CategoryTreeNode[] {
    return nodes.flatMap((node) => {
      if (excludedNameSet.has(node.category.metadata.name)) {
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

export function buildCategoryParentMovePosition(
  categoryName: string,
  previousParentName?: string,
  selectedParentName?: string
): CategoryMovePosition | undefined {
  const normalizedPreviousParentName = previousParentName || undefined;
  const normalizedSelectedParentName = selectedParentName || undefined;

  if (normalizedPreviousParentName === normalizedSelectedParentName) {
    return undefined;
  }

  return {
    name: categoryName,
    parentName: normalizedSelectedParentName,
    beforeName: undefined,
  };
}

export function buildCategoryPositionRequest(
  previousTree: CategoryTreeNode[],
  currentTree: CategoryTreeNode[]
): CategoryMovePosition | undefined {
  const previousPositions = flattenCategoryTreePositions(previousTree);
  const currentPositions = flattenCategoryTreePositions(currentTree);
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

function flattenCategoryTreePositions(tree: CategoryTreeNode[]) {
  const positions = new Map<string, TreeNodePosition>();

  function collect(nodes: CategoryTreeNode[], parentName?: string) {
    nodes.forEach((node, index) => {
      const name = node.category.metadata.name;
      positions.set(name, {
        parentName,
        beforeName: nodes[index + 1]?.category.metadata.name,
      });
      collect(node.children, name);
    });
  }

  collect(tree);
  return positions;
}

function flattenTreeNodes(tree: CategoryTreeNode[]): CategoryTreeNode[] {
  const nodes: CategoryTreeNode[] = [];

  function collect(current: CategoryTreeNode[]) {
    current.forEach((node) => {
      nodes.push(node);
      collect(node.children);
    });
  }

  collect(tree);
  return nodes;
}

function sameTreeAfterMove(
  previousTree: CategoryTreeNode[],
  currentTree: CategoryTreeNode[],
  move: CategoryMovePosition
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

function cloneTree(tree: CategoryTreeNode[]): CategoryTreeNode[] {
  return tree.map((node) => ({
    category: node.category,
    children: cloneTree(node.children),
  }));
}

function detachNode(
  nodes: CategoryTreeNode[],
  name: string
): CategoryTreeNode | undefined {
  const index = nodes.findIndex((node) => node.category.metadata.name === name);
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
  nodes: CategoryTreeNode[],
  movedNode: CategoryTreeNode,
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
    (node) => node.category.metadata.name === beforeName
  );
  if (index < 0) {
    return false;
  }
  siblings.splice(index, 0, movedNode);
  return true;
}

function findNode(
  nodes: CategoryTreeNode[],
  name: string
): CategoryTreeNode | undefined {
  for (const node of nodes) {
    if (node.category.metadata.name === name) {
      return node;
    }
    const child = findNode(node.children, name);
    if (child) {
      return child;
    }
  }
}

function sameTreeNames(left: CategoryTreeNode[], right: CategoryTreeNode[]) {
  if (left.length !== right.length) {
    return false;
  }

  return left.every((leftNode, index) => {
    const rightNode = right[index];
    return (
      leftNode.category.metadata.name === rightNode.category.metadata.name &&
      sameTreeNames(leftNode.children, rightNode.children)
    );
  });
}

export const getCategoryPath = (
  categories: CategoryTreeNode[],
  name: string,
  path: CategoryTreeNode[] = []
): CategoryTreeNode[] | undefined => {
  for (const category of categories) {
    if (category.category.metadata.name === name) {
      return path.concat([category]);
    }

    if (category.children.length) {
      const found = getCategoryPath(
        category.children,
        name,
        path.concat([category])
      );
      if (found) {
        return found;
      }
    }
  }
};
