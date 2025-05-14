import type { MenuItem } from "@halo-dev/api-client";
import { describe, expect, it } from "vitest";
import type { MenuTreeItem } from "../index";
import {
  buildMenuItemsTree,
  convertMenuTreeItemToMenuItem,
  convertTreeToMenuItems,
  getChildrenNames,
  resetMenuItemsTreePriority,
  sortMenuItemsTree,
} from "../index";

const rawMenuItems: MenuItem[] = [
  {
    spec: {
      displayName: "文章分类",
      href: "https://ryanc.cc/categories",
      children: [
        "caeef383-3828-4039-9114-6f9ad3b4a37e",
        "ded1943d-9fdb-4563-83ee-2f04364872e0",
      ],
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
      children: ["96b636bb-3e4a-44d1-8ea7-f9da9e876f45"],
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
  it("should match snapshot", () => {
    const tree = buildMenuItemsTree(rawMenuItems);
    expect(tree).toMatchSnapshot();
  });

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
});

describe("convertTreeToMenuItems", () => {
  it("will match snapshot", function () {
    const menuTreeItems = buildMenuItemsTree(rawMenuItems);
    expect(convertTreeToMenuItems(menuTreeItems)).toMatchSnapshot();
  });
  it("should handle empty input", () => {
    expect(convertTreeToMenuItems([])).toEqual([]);
  });
});

describe("sortMenuItemsTree", () => {
  it("will match snapshot", () => {
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
    expect(sortMenuItemsTree(tree)).toMatchSnapshot();
  });
});

describe("resetMenuItemsTreePriority", () => {
  it("should match snapshot", function () {
    expect(
      resetMenuItemsTreePriority(buildMenuItemsTree(rawMenuItems))
    ).toMatchSnapshot();
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
  it("should match snapshot", () => {
    const menuTreeItems = buildMenuItemsTree(rawMenuItems);
    expect(convertMenuTreeItemToMenuItem(menuTreeItems[1])).toMatchSnapshot();
    expect(
      convertMenuTreeItemToMenuItem(menuTreeItems[1].children[1])
    ).toMatchSnapshot();
  });
  it("should return correct MenuItem", () => {
    const menuTreeItems = buildMenuItemsTree(rawMenuItems);
    expect(
      convertMenuTreeItemToMenuItem(menuTreeItems[1]).spec.displayName
    ).toBe("文章分类");
    expect(
      convertMenuTreeItemToMenuItem(menuTreeItems[1]).spec.children
    ).toStrictEqual([
      "caeef383-3828-4039-9114-6f9ad3b4a37e",
      "ded1943d-9fdb-4563-83ee-2f04364872e0",
    ]);
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
    expect(convertMenuTreeItemToMenuItem(node)).toMatchSnapshot();
  });
});
