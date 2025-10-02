import type { Category } from "@halo-dev/api-client";
import { cloneDeep } from "lodash-es";

export interface CategoryTreeNode extends Category {
  children: CategoryTreeNode[];
}

export function buildCategoriesTree(
  categories: Category[]
): CategoryTreeNode[] {
  const categoriesToUpdate = cloneDeep(categories);

  const categoriesMap: Record<string, CategoryTreeNode> = {};
  const parentMap: Record<string, string> = {};

  categoriesToUpdate.forEach((category) => {
    categoriesMap[category.metadata.name] = {
      ...category,
      children: [],
    } as CategoryTreeNode;

    if (category.spec.children) {
      category.spec.children.forEach((child) => {
        parentMap[child] = category.metadata.name;
      });
    }
  });

  categoriesToUpdate.forEach((category) => {
    const parentName = parentMap[category.metadata.name];
    if (parentName && categoriesMap[parentName]) {
      categoriesMap[parentName].children.push(
        categoriesMap[category.metadata.name]
      );
    }
  });

  const categoriesTree = Object.values(categoriesMap).filter(
    (node) => parentMap[node.metadata.name] === undefined
  );

  return sortCategoriesTree(categoriesTree);
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

  const convertCategory = (node: CategoryTreeNode | undefined) => {
    if (!node) {
      return;
    }

    const children = node.children || [];

    categoriesMap.set(node.metadata.name, {
      ...node,
      spec: {
        ...node.spec,
        children: children.map((child) => child.metadata.name),
      },
    });

    children.forEach((child) => {
      convertCategory(child);
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
  const childNames = categoryTree.children.map((child) => child.metadata.name);

  const { children: _, ...categoryWithoutChildren } = categoryTree;

  return {
    ...categoryWithoutChildren,
    spec: {
      ...categoryTree.spec,
      children: childNames,
    },
  };
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
