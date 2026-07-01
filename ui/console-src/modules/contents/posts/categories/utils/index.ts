import type { Category, JsonPatchInner } from "@halo-dev/api-client";
import { cloneDeep } from "es-toolkit";

export interface CategoryTreeNode extends Category {
  children: CategoryTreeNode[];
}

export function buildCategoriesTree(
  categories: Category[]
): CategoryTreeNode[] {
  const categoriesToUpdate = cloneDeep(categories);

  const categoriesMap: Record<string, CategoryTreeNode> = {};

  categoriesToUpdate.forEach((category) => {
    categoriesMap[category.metadata.name] = {
      ...category,
      children: [],
    } as CategoryTreeNode;
  });

  const parentMap = validParentMap(categoriesMap);
  const cyclicNames = getCyclicNames(parentMap);

  Object.values(categoriesMap).forEach((category) => {
    const parentName = parentMap[category.metadata.name];
    if (parentName && !cyclicNames.has(category.metadata.name)) {
      categoriesMap[parentName].children.push(category);
    }
  });

  const categoriesTree = Object.values(categoriesMap).filter((node) => {
    return (
      !parentMap[node.metadata.name] || cyclicNames.has(node.metadata.name)
    );
  });

  return sortCategoriesTree(categoriesTree);
}

function validParentMap(categoriesMap: Record<string, CategoryTreeNode>) {
  const parentMap: Record<string, string> = {};

  Object.values(categoriesMap).forEach((category) => {
    const parentName = category.spec.parent;
    const categoryName = category.metadata.name;
    if (
      parentName &&
      parentName !== categoryName &&
      categoriesMap[parentName]
    ) {
      parentMap[categoryName] = parentName;
    }
  });

  return parentMap;
}

function getCyclicNames(parentMap: Record<string, string>) {
  const cyclicNames = new Set<string>();

  Object.keys(parentMap).forEach((name) => {
    const path: string[] = [];
    const visiting = new Set<string>();
    let current: string | undefined = name;
    while (current && parentMap[current]) {
      if (visiting.has(current)) {
        path.slice(path.indexOf(current)).forEach((item) => {
          cyclicNames.add(item);
        });
        break;
      }
      visiting.add(current);
      path.push(current);
      current = parentMap[current];
    }
  });

  return cyclicNames;
}

export function sortCategoriesTree(
  categoriesTree: CategoryTreeNode[]
): CategoryTreeNode[] {
  return categoriesTree
    .sort((a, b) => {
      if (a.spec.priority < b.spec.priority) {
        return -1;
      }
      if (a.spec.priority > b.spec.priority) {
        return 1;
      }
      return 0;
    })
    .map((category) => {
      if (category.children && category.children.length) {
        return {
          ...category,
          children: sortCategoriesTree(category.children),
        };
      }
      return category;
    });
}

export function resetCategoriesTreePriority(
  categoriesTree: CategoryTreeNode[]
): CategoryTreeNode[] {
  for (let i = 0; i < categoriesTree.length; i++) {
    categoriesTree[i].spec.priority = i;
    if (categoriesTree[i].children && categoriesTree[i].children.length) {
      resetCategoriesTreePriority(categoriesTree[i].children);
    }
  }
  return categoriesTree;
}

export function convertTreeToCategories(categoriesTree: CategoryTreeNode[]) {
  const categories: Category[] = [];
  const categoriesMap = new Map<string, Category>();

  const convertCategory = (
    node: CategoryTreeNode | undefined,
    parentName?: string
  ) => {
    if (!node) {
      return;
    }

    const children = node.children || [];
    const { children: _, ...categoryWithoutChildren } = node;
    const category = {
      ...categoryWithoutChildren,
      spec: {
        ...categoryWithoutChildren.spec,
      },
    };

    if (parentName) {
      category.spec.parent = parentName;
    } else {
      delete category.spec.parent;
    }

    categoriesMap.set(node.metadata.name, category);

    children.forEach((child) => {
      convertCategory(child, node.metadata.name);
    });
  };

  categoriesTree.forEach((node) => {
    convertCategory(node);
  });

  categoriesMap.forEach((node) => {
    categories.push(node);
  });

  return categories;
}

export function convertCategoryTreeToCategory(
  categoryTree: CategoryTreeNode
): Category {
  const { children: _, ...categoryWithoutChildren } = categoryTree;

  return {
    ...categoryWithoutChildren,
    spec: {
      ...categoryTree.spec,
    },
  };
}

export function createCategoryPatch(
  category: Category,
  originalParentName?: string
): JsonPatchInner[] {
  const jsonPatchInner: JsonPatchInner[] = [];

  if (category.spec.parent) {
    jsonPatchInner.push({
      op: "add",
      path: "/spec/parent",
      value: category.spec.parent,
    });
  } else if (originalParentName) {
    jsonPatchInner.push({
      op: "remove",
      path: "/spec/parent",
    });
  }

  jsonPatchInner.push({
    op: "add",
    path: "/spec/priority",
    value: category.spec.priority || 0,
  });

  return jsonPatchInner;
}

export const getCategoryPath = (
  categories: CategoryTreeNode[],
  name: string,
  path: CategoryTreeNode[] = []
): CategoryTreeNode[] | undefined => {
  for (const category of categories) {
    if (category.metadata && category.metadata.name === name) {
      return path.concat([category]);
    }

    if (category.children && category.children.length) {
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
