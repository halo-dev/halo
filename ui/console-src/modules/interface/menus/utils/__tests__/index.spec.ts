import type { Menu, MenuItem, MenuItemTreeNode } from "@halo-dev/api-client";
import { describe, expect, it } from "vite-plus/test";
import {
  buildMenuItemParentMovePosition,
  buildMenuItemHierarchyPatch,
  buildMenuItemPositionRequest,
  filterMenuItemTreeNodes,
  findFallbackMenuAfterDelete,
  flattenMenuItemTreeNodes,
  getMenuItemTreeNodeChildrenNames,
  getSelectableParentMenuItemTreeNodes,
  resolveClonedParentName,
} from "../index";

const rawMenuItems: MenuItem[] = [
  menuItem("categories", {
    displayName: "文章分类",
    href: "https://ryanc.cc/categories",
    priority: 1,
  }),
  menuItem("halo", {
    displayName: "Halo",
    href: "https://ryanc.cc/categories/halo",
    parent: "categories",
    priority: 0,
  }),
  menuItem("java", {
    displayName: "Java",
    href: "https://ryanc.cc/categories/java",
    parent: "categories",
    priority: 1,
  }),
  menuItem("spring-boot", {
    displayName: "Spring Boot",
    href: "https://ryanc.cc/categories/spring-boot",
    parent: "java",
    priority: 0,
  }),
  menuItem("home", {
    displayName: "首页",
    href: "https://ryanc.cc/",
    priority: 0,
  }),
];

describe("flattenMenuItemTreeNodes", () => {
  it("should flatten tree nodes in display order", () => {
    expect(flattenMenuItemTreeNodes(menuTree()).map(nodeName)).toEqual([
      "home",
      "categories",
      "halo",
      "java",
      "spring-boot",
    ]);
  });

  it("should handle empty input", () => {
    expect(flattenMenuItemTreeNodes([])).toEqual([]);
  });
});

describe("getMenuItemTreeNodeChildrenNames", () => {
  it("should return descendant names", () => {
    const tree = menuTree();

    expect(getMenuItemTreeNodeChildrenNames(tree[0])).toEqual([]);
    expect(getMenuItemTreeNodeChildrenNames(tree[1])).toEqual([
      "halo",
      "java",
      "spring-boot",
    ]);
    expect(getMenuItemTreeNodeChildrenNames(tree[1].children[0])).toEqual([]);
  });
});

describe("getSelectableParentMenuItemTreeNodes", () => {
  it("should include all menu items when creating", () => {
    expect(
      getSelectableParentMenuItemTreeNodes(menuTree()).map(nodeName)
    ).toEqual(["home", "categories", "halo", "java", "spring-boot"]);
  });

  it("should exclude the current item and descendants when editing", () => {
    expect(
      getSelectableParentMenuItemTreeNodes(menuTree(), "categories").map(
        nodeName
      )
    ).toEqual(["home"]);
  });
});

describe("filterMenuItemTreeNodes", () => {
  it("should exclude matching menu items and their child subtrees", () => {
    expect(
      flattenMenuItemTreeNodes(
        filterMenuItemTreeNodes(menuTree(), ["categories"])
      ).map(nodeName)
    ).toEqual(["home"]);
  });
});

describe("buildMenuItemParentMovePosition", () => {
  it("should return undefined when the parent is unchanged", () => {
    expect(
      buildMenuItemParentMovePosition("halo", "categories", "categories")
    ).toBeUndefined();
  });

  it("should append the item to another parent when parent changes", () => {
    expect(
      buildMenuItemParentMovePosition("halo", "categories", "java")
    ).toEqual({
      name: "halo",
      parentName: "java",
      beforeName: undefined,
    });
  });

  it("should move the item to root when selected parent is empty", () => {
    expect(buildMenuItemParentMovePosition("halo", "categories", "")).toEqual({
      name: "halo",
      parentName: undefined,
      beforeName: undefined,
    });
  });
});

describe("buildMenuItemPositionRequest", () => {
  it("should detect a root node moved after its siblings", () => {
    const previousTree = [node("a"), node("b"), node("c")];
    const currentTree = [node("b"), node("c"), node("a")];

    expect(buildMenuItemPositionRequest(previousTree, currentTree)).toEqual({
      name: "a",
      parentName: undefined,
      beforeName: undefined,
    });
  });

  it("should detect a root node moved before a sibling", () => {
    const previousTree = [node("a"), node("b"), node("c")];
    const currentTree = [node("b"), node("a"), node("c")];

    expect(buildMenuItemPositionRequest(previousTree, currentTree)).toEqual({
      name: "b",
      parentName: undefined,
      beforeName: "a",
    });
  });

  it("should detect a node moved under a parent", () => {
    const previousTree = [node("a"), node("b"), node("c")];
    const currentTree = [node("a", [node("b")]), node("c")];

    expect(buildMenuItemPositionRequest(previousTree, currentTree)).toEqual({
      name: "b",
      parentName: "a",
      beforeName: undefined,
    });
  });

  it("should return only the moved item and its target position", () => {
    const previousTree = [node("a"), node("b"), node("c")];
    const currentTree = [node("a", [node("b")]), node("c")];

    const request = buildMenuItemPositionRequest(previousTree, currentTree);

    expect(request).toEqual({
      name: "b",
      parentName: "a",
      beforeName: undefined,
    });
    expect(request).not.toHaveProperty("priority");
    expect(request).not.toHaveProperty("jsonPatchInner");
  });

  it("should detect a child moved to root before a sibling", () => {
    const previousTree = [node("a", [node("b")]), node("c")];
    const currentTree = [node("a"), node("b"), node("c")];

    expect(buildMenuItemPositionRequest(previousTree, currentTree)).toEqual({
      name: "b",
      parentName: undefined,
      beforeName: "c",
    });
  });

  it("should return undefined when tree order is unchanged", () => {
    const tree = menuTree();

    expect(buildMenuItemPositionRequest(tree, tree)).toBeUndefined();
  });
});

describe("findFallbackMenuAfterDelete", () => {
  it("should choose the first available menu after the deleted menu", () => {
    expect(
      findFallbackMenuAfterDelete(
        [menu("primary"), menu("secondary"), menu("footer")],
        "primary"
      )?.metadata.name
    ).toBe("secondary");
  });

  it("should skip deleting menus", () => {
    expect(
      findFallbackMenuAfterDelete(
        [menu("primary"), menu("secondary", true), menu("footer")],
        "primary"
      )?.metadata.name
    ).toBe("footer");
  });

  it("should return undefined when no fallback menu exists", () => {
    expect(findFallbackMenuAfterDelete([menu("primary")], "primary")).toBe(
      undefined
    );
  });
});

describe("resolveClonedParentName", () => {
  it("should resolve cloned parent names", () => {
    const oldToNewNameMap = new Map<string, string>([
      ["categories", "new-parent"],
    ]);

    expect(resolveClonedParentName(rawMenuItems[1], oldToNewNameMap)).toBe(
      "new-parent"
    );
    expect(resolveClonedParentName(rawMenuItems[0], oldToNewNameMap)).toBe(
      undefined
    );
  });
});

describe("buildMenuItemHierarchyPatch", () => {
  it("should add parent when menu item has a parent", () => {
    expect(
      buildMenuItemHierarchyPatch(rawMenuItems[1], "primary")
    ).toStrictEqual([
      {
        op: "add",
        path: "/spec/priority",
        value: 0,
      },
      {
        op: "add",
        path: "/spec/menuName",
        value: "primary",
      },
      {
        op: "add",
        path: "/spec/parent",
        value: "categories",
      },
    ]);
  });

  it("should remove parent when existing child moves to root", () => {
    expect(
      buildMenuItemHierarchyPatch(
        {
          ...rawMenuItems[1],
          spec: {
            ...rawMenuItems[1].spec,
            parent: undefined,
          },
        },
        "primary",
        "old-parent"
      )
    ).toStrictEqual([
      {
        op: "add",
        path: "/spec/priority",
        value: 0,
      },
      {
        op: "add",
        path: "/spec/menuName",
        value: "primary",
      },
      {
        op: "remove",
        path: "/spec/parent",
      },
    ]);
  });

  it("should skip parent operation when root item stays root", () => {
    const patch = buildMenuItemHierarchyPatch(rawMenuItems[0], "primary");

    expect(patch).toStrictEqual([
      {
        op: "add",
        path: "/spec/priority",
        value: 1,
      },
      {
        op: "add",
        path: "/spec/menuName",
        value: "primary",
      },
    ]);
    expect(patch).not.toContainEqual(
      expect.objectContaining({
        path: "/spec/parent",
        value: null,
      })
    );
  });
});

function menuTree(): MenuItemTreeNode[] {
  return [
    node("home"),
    node("categories", [node("halo"), node("java", [node("spring-boot")])]),
  ];
}

function node(
  name: string,
  children: MenuItemTreeNode[] = []
): MenuItemTreeNode {
  return {
    menuItem: menuItem(name),
    children,
  };
}

function nodeName(node: MenuItemTreeNode) {
  return node.menuItem.metadata.name;
}

function menu(name: string, deleting = false): Menu {
  return {
    spec: {
      displayName: name,
      menuItems: [],
    },
    apiVersion: "v1alpha1",
    kind: "Menu",
    metadata: {
      name,
      version: 1,
      creationTimestamp: "2022-08-05T04:19:37.252228Z",
      deletionTimestamp: deleting ? "2022-08-05T04:19:37.252228Z" : undefined,
    },
  };
}

function menuItem(
  name: string,
  spec: Partial<MenuItem["spec"]> = {}
): MenuItem {
  return {
    spec: {
      displayName: name,
      href: "#",
      menuName: "primary",
      children: [],
      priority: 0,
      ...spec,
    },
    apiVersion: "v1alpha1",
    kind: "MenuItem",
    metadata: {
      name,
      version: 1,
      creationTimestamp: "2022-08-05T04:19:37.252228Z",
    },
  };
}
