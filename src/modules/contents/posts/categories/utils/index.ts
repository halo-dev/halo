import type { Category, CategorySpec } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";

export interface CategoryTreeSpec extends Omit<CategorySpec, "children"> {
  children: CategoryTree[];
}

export interface CategoryTree extends Omit<Category, "spec"> {
  spec: CategoryTreeSpec;
}

export function buildCategoriesTree(categories: Category[]): CategoryTree[] {
  const categoriesToUpdate = cloneDeep(categories);

  const categoriesMap = {};
  const parentMap = {};

  categoriesToUpdate.forEach((category) => {
    categoriesMap[category.metadata.name] = category;
    // @ts-ignore
    category.spec.children.forEach((child) => {
      parentMap[child] = category.metadata.name;
    });
    // @ts-ignore
    category.spec.children = [];
  });

  categoriesToUpdate.forEach((category) => {
    const parentName = parentMap[category.metadata.name];
    if (parentName && categoriesMap[parentName]) {
      categoriesMap[parentName].spec.children.push(category);
    }
  });

  const categoriesTree = categoriesToUpdate.filter(
    (node) => parentMap[node.metadata.name] === undefined
  );

  return sortCategoriesTree(categoriesTree);
}

export function sortCategoriesTree(
  categoriesTree: CategoryTree[] | Category[]
): CategoryTree[] {
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
      if (category.spec.children.length) {
        return {
          ...category,
          spec: {
            ...category.spec,
            children: sortCategoriesTree(category.spec.children),
          },
        };
      }
      return category;
    });
}

export function resetCategoriesTreePriority(
  categoriesTree: CategoryTree[]
): CategoryTree[] {
  for (let i = 0; i < categoriesTree.length; i++) {
    categoriesTree[i].spec.priority = i;
    if (categoriesTree[i].spec.children) {
      resetCategoriesTreePriority(categoriesTree[i].spec.children);
    }
  }
  return categoriesTree;
}

export function convertTreeToCategories(categoriesTree: CategoryTree[]) {
  const categories: Category[] = [];
  const categoriesMap = new Map<string, Category>();
  const convertCategory = (node: CategoryTree | undefined) => {
    if (!node) {
      return;
    }
    const children = node.spec.children || [];
    categoriesMap.set(node.metadata.name, {
      ...node,
      spec: {
        ...node.spec,
        // @ts-ignore
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
  categoryTree: CategoryTree
): Category {
  const childNames = categoryTree.spec.children.map(
    (child) => child.metadata.name
  );
  return {
    ...categoryTree,
    spec: {
      ...categoryTree.spec,
      children: childNames,
    },
  };
}

export const getCategoryPath = (
  categories: CategoryTree[],
  name: string,
  path: CategoryTree[] = []
): CategoryTree[] | undefined => {
  for (const category of categories) {
    if (category.metadata && category.metadata.name === name) {
      return path.concat([category]);
    }

    if (category.spec && category.spec.children) {
      const found = getCategoryPath(
        category.spec.children,
        name,
        path.concat([category])
      );
      if (found) {
        return found;
      }
    }
  }
};
