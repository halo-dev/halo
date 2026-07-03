import type { Category, CategoryTreeNode } from "@halo-dev/api-client";
import { describe, expect, it } from "vitest";
import {
  buildCategoryPositionRequest,
  convertCategoryTreeToCategory,
  flattenCategoryTreeNodes,
  getCategoryFromNode,
  getCategoryPath,
} from "../index";

describe("flattenCategoryTreeNodes", () => {
  it("should flatten backend tree nodes in depth-first order", () => {
    const tree = [
      node("root", [node("child", [node("grandchild")])]),
      node("sibling"),
    ];

    expect(flattenCategoryTreeNodes(tree).map(categoryName)).toEqual([
      "root",
      "child",
      "grandchild",
      "sibling",
    ]);
  });

  it("should return an empty list for empty tree", () => {
    expect(flattenCategoryTreeNodes([])).toEqual([]);
  });
});

describe("getCategoryPath", () => {
  it("should return category tree nodes from root to target", () => {
    const tree = [
      node("root", [node("child", [node("grandchild")])]),
      node("sibling"),
    ];

    expect(getCategoryPath(tree, "grandchild")?.map(nodeName)).toEqual([
      "root",
      "child",
      "grandchild",
    ]);
  });

  it("should return undefined when category does not exist", () => {
    expect(getCategoryPath([node("root")], "missing")).toBeUndefined();
  });
});

describe("convertCategoryTreeToCategory", () => {
  it("should return a cloned Category without tree children", () => {
    const treeNode = node("root", [node("child")]);

    const category = convertCategoryTreeToCategory(treeNode);

    expect(category.metadata.name).toBe("root");
    expect("children" in category).toBe(false);

    category.spec.displayName = "changed";

    expect(treeNode.category.spec.displayName).toBe("root");
  });
});

describe("getCategoryFromNode", () => {
  it("should resolve plain Categories and backend tree nodes", () => {
    const category = categoryOf("plain");
    const treeNode = node("tree");

    expect(getCategoryFromNode(category).metadata.name).toBe("plain");
    expect(getCategoryFromNode(treeNode).metadata.name).toBe("tree");
  });
});

describe("buildCategoryPositionRequest", () => {
  it("should return the moved category and its target child position", () => {
    const previousTree = [node("a"), node("b"), node("c")];
    const currentTree = [node("a", [node("b")]), node("c")];

    expect(buildCategoryPositionRequest(previousTree, currentTree)).toEqual({
      name: "b",
      parentName: "a",
      beforeName: undefined,
    });
  });

  it("should detect a child moved to root before a sibling", () => {
    const previousTree = [node("a", [node("b")]), node("c")];
    const currentTree = [node("a"), node("b"), node("c")];

    expect(buildCategoryPositionRequest(previousTree, currentTree)).toEqual({
      name: "b",
      parentName: undefined,
      beforeName: "c",
    });
  });

  it("should detect sibling insertion before another category", () => {
    const previousTree = [node("a"), node("b"), node("c")];
    const currentTree = [node("b"), node("a"), node("c")];

    expect(buildCategoryPositionRequest(previousTree, currentTree)).toEqual({
      name: "b",
      parentName: undefined,
      beforeName: "a",
    });
  });

  it("should return undefined when tree order is unchanged", () => {
    const tree = [node("a", [node("b")]), node("c")];

    expect(buildCategoryPositionRequest(tree, tree)).toBeUndefined();
  });

  it("should return undefined when changes cannot be explained by one move", () => {
    const previousTree = [node("a"), node("b"), node("c")];
    const currentTree = [node("c"), node("b"), node("a")];

    expect(
      buildCategoryPositionRequest(previousTree, currentTree)
    ).toBeUndefined();
  });

  it("should ignore newly inserted nodes and return undefined", () => {
    const previousTree = [node("a")];
    const currentTree = [node("a"), node("b")];

    expect(
      buildCategoryPositionRequest(previousTree, currentTree)
    ).toBeUndefined();
  });
});

function node(
  name: string,
  children: CategoryTreeNode[] = []
): CategoryTreeNode {
  return {
    category: categoryOf(name),
    children,
  };
}

function categoryOf(name: string): Category {
  return {
    spec: {
      displayName: name,
      slug: name,
      priority: 0,
    },
    status: {},
    apiVersion: "content.halo.run/v1alpha1",
    kind: "Category",
    metadata: {
      name,
    },
  };
}

function nodeName(treeNode: CategoryTreeNode) {
  return treeNode.category.metadata.name;
}

function categoryName(category: Category) {
  return category.metadata.name;
}
