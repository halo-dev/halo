import type { MenuGroupType } from "@halo-dev/console-shared";

export const coreMenuGroups: MenuGroupType[] = [
  {
    id: "dashboard",
    name: undefined,
    priority: 0,
  },
  {
    id: "content",
    name: "core.sidebar.menu.groups.content",
    priority: 1,
  },
  {
    id: "interface",
    name: "core.sidebar.menu.groups.interface",
    priority: 2,
  },
  {
    id: "system",
    name: "core.sidebar.menu.groups.system",
    priority: 3,
  },
  {
    id: "tool",
    name: "core.sidebar.menu.groups.tool",
    priority: 4,
  },
];
