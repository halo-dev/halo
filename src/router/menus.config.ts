import {
  IconBookRead,
  IconDashboard,
  IconFolder,
  IconMessage,
  IconUserSettings,
} from "@halo-dev/components";

import type { MenuGroupType, MenuItemType } from "@halo-dev/admin-shared";

export const menus: MenuGroupType[] = [
  {
    name: "",
    items: [],
  },
  {
    name: "内容",
    items: [],
  },
  {
    name: "外观",
    items: [],
  },
  {
    name: "系统",
    items: [],
  },
];

export const minimenus: MenuItemType[] = [
  {
    name: "仪表盘",
    path: "/dashboard",
    icon: IconDashboard,
  },
  {
    name: "文章",
    path: "/posts",
    icon: IconBookRead,
  },
  {
    name: "评论",
    path: "/comments",
    icon: IconMessage,
  },
  {
    name: "附件",
    path: "/attachments",
    icon: IconFolder,
  },
  {
    name: "用户",
    path: "/users/profile/detail",
    icon: IconUserSettings,
  },
];

export function registerMenu(group: string | undefined, menu: MenuItemType) {
  const groupIndex = menus.findIndex((g) => g.name === group);
  if (groupIndex !== -1) {
    menus[groupIndex].items.push(menu);
    return;
  }
  menus.push({
    name: group,
    items: [menu],
  });
}

export type { MenuItemType, MenuGroupType };

export default menus;
