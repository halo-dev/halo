import type { Category } from "@halo-dev/api-client";
import { describe, expect, it } from "vitest";
import {
  buildCategoriesTree,
  convertCategoryTreeToCategory,
  convertTreeToCategories,
  getCategoryPath,
  resetCategoriesTreePriority,
  sortCategoriesTree,
  type CategoryTreeNode,
} from "../index";

function createMockCategory(
  name: string,
  priority = 0,
  children: string[] = []
): Category {
  return {
    metadata: {
      name,
      annotations: {},
      labels: {},
      version: 1,
      creationTimestamp: new Date().toISOString(),
    },
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Category",
    spec: {
      displayName: `Category ${name}`,
      slug: name,
      description: `Description for ${name}`,
      cover: "",
      template: "",
      priority: priority,
      children: children,
    },
  };
}

function createMockCategoryTreeNode(
  name: string,
  priority = 0,
  children: CategoryTreeNode[] = []
): CategoryTreeNode {
  return {
    metadata: {
      name,
      annotations: {},
      labels: {},
      version: 1,
      creationTimestamp: new Date().toISOString(),
    },
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Category",
    spec: {
      displayName: `Category ${name}`,
      slug: name,
      description: `Description for ${name}`,
      cover: "",
      template: "",
      priority: priority,
      children: children.map((child) => child.metadata.name),
    },
    children: children,
  };
}

describe("buildCategoriesTree", () => {
  it("should convert flat category array to tree structure", () => {
    // Prepare test data
    const categories: Category[] = [
      createMockCategory("parent1", 0, ["child1", "child2"]),
      createMockCategory("child1", 0),
      createMockCategory("child2", 1),
      createMockCategory("parent2", 1, ["child3"]),
      createMockCategory("child3", 0),
    ];

    // Execute test
    const result = buildCategoriesTree(categories);

    // Verify results
    expect(result.length).toBe(2); // Should have two root nodes
    expect(result[0].metadata.name).toBe("parent1"); // First root node should be parent1
    expect(result[1].metadata.name).toBe("parent2"); // Second root node should be parent2
    expect(result[0].children.length).toBe(2); // parent1 should have two children
    expect(result[1].children.length).toBe(1); // parent2 should have one child
    expect(result[0].children[0].metadata.name).toBe("child1");
    expect(result[0].children[1].metadata.name).toBe("child2");
    expect(result[1].children[0].metadata.name).toBe("child3");
  });

  it("should handle empty array input", () => {
    const result = buildCategoriesTree([]);
    expect(result).toEqual([]);
  });

  it("should handle categories without parent-child relationships", () => {
    const categories: Category[] = [
      createMockCategory("category1", 0),
      createMockCategory("category2", 1),
      createMockCategory("category3", 2),
    ];

    const result = buildCategoriesTree(categories);

    expect(result.length).toBe(3);
    expect(result.every((node) => node.children.length === 0)).toBe(true);
  });

  it("should handle multi-level nested category structure", () => {
    const categories: Category[] = [
      createMockCategory("root", 0, ["level1"]),
      createMockCategory("level1", 0, ["level2"]),
      createMockCategory("level2", 0, ["level3"]),
      createMockCategory("level3", 0),
    ];

    const result = buildCategoriesTree(categories);

    expect(result.length).toBe(1);
    expect(result[0].metadata.name).toBe("root");
    expect(result[0].children[0].metadata.name).toBe("level1");
    expect(result[0].children[0].children[0].metadata.name).toBe("level2");
    expect(result[0].children[0].children[0].children[0].metadata.name).toBe(
      "level3"
    );
  });
});

describe("sortCategoriesTree", () => {
  it("should sort category tree by priority", () => {
    const categoriesTree: CategoryTreeNode[] = [
      createMockCategoryTreeNode("node3", 2),
      createMockCategoryTreeNode("node1", 0),
      createMockCategoryTreeNode("node2", 1),
    ];

    const result = sortCategoriesTree(categoriesTree);

    expect(result[0].metadata.name).toBe("node1");
    expect(result[1].metadata.name).toBe("node2");
    expect(result[2].metadata.name).toBe("node3");
  });

  it("should recursively sort child nodes", () => {
    const categoriesTree: CategoryTreeNode[] = [
      createMockCategoryTreeNode("parent1", 0, [
        createMockCategoryTreeNode("child3", 2),
        createMockCategoryTreeNode("child1", 0),
        createMockCategoryTreeNode("child2", 1),
      ]),
    ];

    const result = sortCategoriesTree(categoriesTree);

    expect(result[0].children[0].metadata.name).toBe("child1");
    expect(result[0].children[1].metadata.name).toBe("child2");
    expect(result[0].children[2].metadata.name).toBe("child3");
  });

  it("should handle empty array input", () => {
    const result = sortCategoriesTree([]);
    expect(result).toEqual([]);
  });
});

describe("resetCategoriesTreePriority", () => {
  it("should reset priority values of all nodes in the tree", () => {
    const categoriesTree: CategoryTreeNode[] = [
      createMockCategoryTreeNode("node1", 5),
      createMockCategoryTreeNode("node2", 10),
      createMockCategoryTreeNode("node3", 15),
    ];

    const result = resetCategoriesTreePriority(categoriesTree);

    expect(result[0].spec.priority).toBe(0);
    expect(result[1].spec.priority).toBe(1);
    expect(result[2].spec.priority).toBe(2);
  });

  it("should recursively reset child node priorities", () => {
    const categoriesTree: CategoryTreeNode[] = [
      createMockCategoryTreeNode("parent", 5, [
        createMockCategoryTreeNode("child1", 10),
        createMockCategoryTreeNode("child2", 15),
      ]),
    ];

    const result = resetCategoriesTreePriority(categoriesTree);

    expect(result[0].spec.priority).toBe(0);
    expect(result[0].children[0].spec.priority).toBe(0);
    expect(result[0].children[1].spec.priority).toBe(1);
  });

  it("should handle empty array input", () => {
    const result = resetCategoriesTreePriority([]);
    expect(result).toEqual([]);
  });
});

describe("convertTreeToCategories", () => {
  it("should convert tree structure back to flat category array", () => {
    const child1 = createMockCategoryTreeNode("child1", 0);
    const child2 = createMockCategoryTreeNode("child2", 1);
    const parent = createMockCategoryTreeNode("parent", 0, [child1, child2]);
    const categoriesTree: CategoryTreeNode[] = [parent];

    const result = convertTreeToCategories(categoriesTree);

    expect(result.length).toBe(3);

    // Verify parent node
    const parentCategory = result.find((c) => c.metadata.name === "parent");
    expect(parentCategory).toBeDefined();
    expect(parentCategory?.spec.children).toContain("child1");
    expect(parentCategory?.spec.children).toContain("child2");

    // Verify child nodes
    const child1Category = result.find((c) => c.metadata.name === "child1");
    const child2Category = result.find((c) => c.metadata.name === "child2");
    expect(child1Category).toBeDefined();
    expect(child2Category).toBeDefined();
    expect(child1Category?.spec.children).toEqual([]);
    expect(child2Category?.spec.children).toEqual([]);
  });

  it("should handle multi-level nested structure", () => {
    const level3 = createMockCategoryTreeNode("level3", 0);
    const level2 = createMockCategoryTreeNode("level2", 0, [level3]);
    const level1 = createMockCategoryTreeNode("level1", 0, [level2]);
    const root = createMockCategoryTreeNode("root", 0, [level1]);
    const categoriesTree: CategoryTreeNode[] = [root];

    const result = convertTreeToCategories(categoriesTree);

    expect(result.length).toBe(4);

    const rootCategory = result.find((c) => c.metadata.name === "root");
    const level1Category = result.find((c) => c.metadata.name === "level1");
    const level2Category = result.find((c) => c.metadata.name === "level2");
    const level3Category = result.find((c) => c.metadata.name === "level3");

    expect(rootCategory?.spec.children).toContain("level1");
    expect(level1Category?.spec.children).toContain("level2");
    expect(level2Category?.spec.children).toContain("level3");
    expect(level3Category?.spec.children).toEqual([]);
  });

  it("should handle empty array input", () => {
    const result = convertTreeToCategories([]);
    expect(result).toEqual([]);
  });
});

describe("convertCategoryTreeToCategory", () => {
  it("should convert a single tree node to a category object", () => {
    const child1 = createMockCategoryTreeNode("child1", 0);
    const child2 = createMockCategoryTreeNode("child2", 1);
    const parent = createMockCategoryTreeNode("parent", 0, [child1, child2]);

    const result = convertCategoryTreeToCategory(parent);

    expect(result.metadata.name).toBe("parent");
    expect(result.spec.children).toContain("child1");
    expect(result.spec.children).toContain("child2");
    expect(result.spec.children?.length).toBe(2);
    // eslint-disable-next-line
    expect((result as any).children).toBeUndefined();
  });

  it("should handle nodes without children", () => {
    const node = createMockCategoryTreeNode("node", 0);

    const result = convertCategoryTreeToCategory(node);

    expect(result.metadata.name).toBe("node");
    expect(result.spec.children).toEqual([]);
  });
});

describe("getCategoryPath", () => {
  it("should return path from root to specified category", () => {
    const level3 = createMockCategoryTreeNode("level3", 0);
    const level2 = createMockCategoryTreeNode("level2", 0, [level3]);
    const level1 = createMockCategoryTreeNode("level1", 0, [level2]);
    const root = createMockCategoryTreeNode("root", 0, [level1]);
    const categoriesTree: CategoryTreeNode[] = [root];

    const result = getCategoryPath(categoriesTree, "level3");

    expect(result).toBeDefined();
    expect(result?.length).toBe(4);
    expect(result?.[0].metadata.name).toBe("root");
    expect(result?.[1].metadata.name).toBe("level1");
    expect(result?.[2].metadata.name).toBe("level2");
    expect(result?.[3].metadata.name).toBe("level3");
  });

  it("should handle case when category is not found", () => {
    const categoriesTree: CategoryTreeNode[] = [
      createMockCategoryTreeNode("node1", 0),
      createMockCategoryTreeNode("node2", 1),
    ];

    const result = getCategoryPath(categoriesTree, "nonexistent");

    expect(result).toBeUndefined();
  });

  it("should handle multiple branches", () => {
    const child1 = createMockCategoryTreeNode("child1", 0);
    const child2 = createMockCategoryTreeNode("child2", 1);
    const target = createMockCategoryTreeNode("target", 0);
    const branch1 = createMockCategoryTreeNode("branch1", 0, [child1, child2]);
    const branch2 = createMockCategoryTreeNode("branch2", 1, [target]);
    const root = createMockCategoryTreeNode("root", 0, [branch1, branch2]);
    const categoriesTree: CategoryTreeNode[] = [root];

    const result = getCategoryPath(categoriesTree, "target");

    expect(result).toBeDefined();
    expect(result?.length).toBe(3);
    expect(result?.[0].metadata.name).toBe("root");
    expect(result?.[1].metadata.name).toBe("branch2");
    expect(result?.[2].metadata.name).toBe("target");
  });

  it("should handle empty array input", () => {
    const result = getCategoryPath([], "any");
    expect(result).toBeUndefined();
  });
});
