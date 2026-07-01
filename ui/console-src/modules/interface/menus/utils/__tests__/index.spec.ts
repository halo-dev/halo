import type { MenuItem } from "@halo-dev/api-client";
import { describe, expect, it } from "vite-plus/test";
import type { MenuTreeItem } from "../index";
import {
  buildMenuItemHierarchyPatch,
  buildMenuItemsTree,
  convertMenuTreeItemToMenuItem,
  convertTreeToMenuItems,
  getChildrenNames,
  resetMenuItemsTreePriority,
  resolveClonedParentName,
  sortMenuItemsTree,
} from "../index";

const rawMenuItems: MenuItem[] = [
  {
    spec: {
      displayName: "文章分类",
      href: "https://ryanc.cc/categories",
      menuName: "primary",
      children: [],
      priority: 1,
    },
    apiVersion: "v1alpha1",
    kind: "MenuItem",
    metadata: {
      name: "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
      version: 12,
      creationTimestamp: "2022-08-05T04:19:37.252228Z",
    },
  },
  {
    spec: {
      displayName: "Halo",
      href: "https://ryanc.cc/categories/halo",
      menuName: "primary",
      parent: "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
      children: [],
      priority: 0,
    },
    apiVersion: "v1alpha1",
    kind: "MenuItem",
    metadata: {
      name: "caeef383-3828-4039-9114-6f9ad3b4a37e",
      version: 4,
      creationTimestamp: "2022-07-28T06:50:32.777556Z",
    },
  },
  {
    spec: {
      displayName: "Java",
      href: "https://ryanc.cc/categories/java",
      menuName: "primary",
      parent: "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
      children: [],
      priority: 1,
    },
    apiVersion: "v1alpha1",
    kind: "MenuItem",
    metadata: {
      name: "ded1943d-9fdb-4563-83ee-2f04364872e0",
      version: 1,
      creationTimestamp: "2022-08-05T04:22:03.377364Z",
    },
  },
  {
    spec: {
      displayName: "Spring Boot",
      href: "https://ryanc.cc/categories/spring-boot",
      menuName: "primary",
      parent: "ded1943d-9fdb-4563-83ee-2f04364872e0",
      children: [],
      priority: 0,
    },
    apiVersion: "v1alpha1",
    kind: "MenuItem",
    metadata: {
      name: "96b636bb-3e4a-44d1-8ea7-f9da9e876f45",
      version: 1,
      creationTimestamp: "2022-08-05T04:22:03.377364Z",
    },
  },
  {
    spec: {
      displayName: "首页",
      href: "https://ryanc.cc/",
      menuName: "primary",
      children: [],
      priority: 0,
    },
    apiVersion: "v1alpha1",
    kind: "MenuItem",
    metadata: {
      name: "411a3639-bf0d-4266-9cfb-14184259dab5",
      version: 1,
      creationTimestamp: "2022-08-05T04:22:03.377364Z",
    },
  },
];

describe("buildMenuItemsTree", () => {
  it("should be sorted correctly and children at top level", () => {
    const menuItems = buildMenuItemsTree(rawMenuItems);
    expect(menuItems[0].spec.priority).toBe(0);
    expect(menuItems[1].spec.priority).toBe(1);
    expect(menuItems[1].children[0].spec.priority).toBe(0);
    expect(menuItems[1].children[1].spec.priority).toBe(1);
    expect(menuItems[1].children[1].children[0].spec.priority).toBe(0);
    expect(menuItems[0].spec.displayName).toBe("首页");
    expect(menuItems[1].spec.displayName).toBe("文章分类");
    expect(menuItems[1].children[0].spec.displayName).toBe("Halo");
    expect(menuItems[1].children[1].spec.displayName).toBe("Java");
    expect(menuItems[1].children[1].children[0].spec.displayName).toBe(
      "Spring Boot"
    );
  });

  it("should handle empty input", () => {
    expect(buildMenuItemsTree([])).toEqual([]);
  });

  it("should render invalid parents as roots", () => {
    const roots = buildMenuItemsTree([
      {
        ...rawMenuItems[0],
        metadata: { ...rawMenuItems[0].metadata, name: "root" },
        spec: { ...rawMenuItems[0].spec, parent: "missing", priority: 0 },
      },
      {
        ...rawMenuItems[1],
        metadata: { ...rawMenuItems[1].metadata, name: "child" },
        spec: { ...rawMenuItems[1].spec, parent: "root", priority: 0 },
      },
      {
        ...rawMenuItems[2],
        metadata: { ...rawMenuItems[2].metadata, name: "self" },
        spec: { ...rawMenuItems[2].spec, parent: "self", priority: 1 },
      },
      {
        ...rawMenuItems[3],
        metadata: { ...rawMenuItems[3].metadata, name: "outside" },
        spec: { ...rawMenuItems[3].spec, parent: "other", priority: 2 },
      },
    ]);
    const root = roots.find((item) => item.metadata.name === "root");

    expect(roots.map((item) => item.metadata.name)).toEqual([
      "root",
      "self",
      "outside",
    ]);
    expect(root?.children[0].metadata.name).toBe("child");
  });
});

describe("convertTreeToMenuItems", () => {
  it("should flatten tree with parent references", function () {
    const menuTreeItems = buildMenuItemsTree(rawMenuItems);
    const items = convertTreeToMenuItems(menuTreeItems, "primary");
    expect(
      items.map((item) => [
        item.metadata.name,
        item.spec.menuName,
        item.spec.parent,
      ])
    ).toEqual([
      ["411a3639-bf0d-4266-9cfb-14184259dab5", "primary", undefined],
      ["40e4ba86-5c7e-43c2-b4c0-cee376d26564", "primary", undefined],
      [
        "caeef383-3828-4039-9114-6f9ad3b4a37e",
        "primary",
        "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
      ],
      [
        "ded1943d-9fdb-4563-83ee-2f04364872e0",
        "primary",
        "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
      ],
      [
        "96b636bb-3e4a-44d1-8ea7-f9da9e876f45",
        "primary",
        "ded1943d-9fdb-4563-83ee-2f04364872e0",
      ],
    ]);
  });
  it("should handle empty input", () => {
    expect(convertTreeToMenuItems([])).toEqual([]);
  });
});

describe("sortMenuItemsTree", () => {
  it("should sort nested items by priority", () => {
    const tree: MenuTreeItem[] = [
      {
        apiVersion: "v1alpha1",
        kind: "MenuItem",
        metadata: {
          creationTimestamp: "2022-08-05T04:19:37.252228Z",
          name: "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
          version: 12,
        },
        spec: {
          priority: 0,
          displayName: "文章分类",
          href: "https://ryanc.cc/categories",
        },
        children: [
          {
            apiVersion: "v1alpha1",
            kind: "MenuItem",
            metadata: {
              creationTimestamp: "2022-07-28T06:50:32.777556Z",
              name: "caeef383-3828-4039-9114-6f9ad3b4a37e",
              version: 4,
            },
            spec: {
              priority: 1,
              displayName: "Halo",
              href: "https://ryanc.cc/categories/halo",
            },
            children: [],
          },
          {
            apiVersion: "v1alpha1",
            kind: "MenuItem",
            metadata: {
              creationTimestamp: "2022-08-05T04:22:03.377364Z",
              name: "ded1943d-9fdb-4563-83ee-2f04364872e0",
              version: 0,
            },
            spec: {
              priority: 0,
              displayName: "Java",
              href: "https://ryanc.cc/categories/java",
            },
            children: [],
          },
        ],
      },
    ];
    const sorted = sortMenuItemsTree(tree);
    expect(sorted[0].children.map((child) => child.spec.displayName)).toEqual([
      "Java",
      "Halo",
    ]);
  });
});

describe("resetMenuItemsTreePriority", () => {
  it("should reset priorities by sibling order", function () {
    const menuTreeItems = resetMenuItemsTreePriority(
      buildMenuItemsTree(rawMenuItems)
    );

    expect(menuTreeItems.map((item) => item.spec.priority)).toEqual([0, 1]);
    expect(menuTreeItems[1].children.map((item) => item.spec.priority)).toEqual(
      [0, 1]
    );
    expect(menuTreeItems[1].children[1].children[0].spec.priority).toBe(0);
  });
});

describe("getChildrenNames", () => {
  it("should return correct names", () => {
    const menuTreeItems = buildMenuItemsTree(rawMenuItems);
    expect(getChildrenNames(menuTreeItems[0])).toEqual([]);
    expect(getChildrenNames(menuTreeItems[1])).toEqual([
      "caeef383-3828-4039-9114-6f9ad3b4a37e",
      "ded1943d-9fdb-4563-83ee-2f04364872e0",
      "96b636bb-3e4a-44d1-8ea7-f9da9e876f45",
    ]);
    expect(getChildrenNames(menuTreeItems[1].children[0])).toEqual([]);
  });
  it("should handle empty children", () => {
    const node: MenuTreeItem = {
      apiVersion: "v1alpha1",
      kind: "MenuItem",
      metadata: {
        name: "test",
        version: 1,
        creationTimestamp: "2022-01-01T00:00:00Z",
      },
      spec: {
        displayName: "test",
        href: "#",
        children: [],
        priority: 0,
      },
      children: [],
    };
    expect(getChildrenNames(node)).toEqual([]);
  });
});

describe("convertMenuTreeItemToMenuItem", () => {
  it("should return correct MenuItem", () => {
    const menuTreeItems = buildMenuItemsTree(rawMenuItems);
    const menuItem = convertMenuTreeItemToMenuItem(menuTreeItems[1]);

    expect("children" in menuItem).toBe(false);
    expect(menuItem.spec.displayName).toBe("文章分类");
    expect(menuItem.spec.children).toStrictEqual([]);
  });
  it("should handle node with empty children", () => {
    const node: MenuTreeItem = {
      apiVersion: "v1alpha1",
      kind: "MenuItem",
      metadata: {
        name: "test",
        version: 1,
        creationTimestamp: "2022-01-01T00:00:00Z",
      },
      spec: {
        displayName: "test",
        href: "#",
        children: [],
        priority: 0,
      },
      children: [],
    };
    expect("children" in convertMenuTreeItemToMenuItem(node)).toBe(false);
  });
});

describe("resolveClonedParentName", () => {
  it("should resolve cloned parent names", () => {
    const oldToNewNameMap = new Map<string, string>([
      ["40e4ba86-5c7e-43c2-b4c0-cee376d26564", "new-parent"],
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
        value: "40e4ba86-5c7e-43c2-b4c0-cee376d26564",
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
