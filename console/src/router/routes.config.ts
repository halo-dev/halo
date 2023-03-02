import type { RouteRecordRaw } from "vue-router";
import NotFound from "@/views/exceptions/NotFound.vue";
import Forbidden from "@/views/exceptions/Forbidden.vue";
import BasicLayout from "@/layouts/BasicLayout.vue";
import Setup from "@/views/system/Setup.vue";
import type { MenuGroupType } from "@halo-dev/console-shared";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/:pathMatch(.*)*",
    component: BasicLayout,
    children: [{ path: "", name: "NotFound", component: NotFound }],
  },
  {
    path: "/403",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Forbidden",
        component: Forbidden,
      },
    ],
  },
  {
    path: "/setup",
    name: "Setup",
    component: Setup,
    meta: {
      title: "系统初始化",
    },
  },
];

export const coreMenuGroups: MenuGroupType[] = [
  {
    id: "dashboard",
    name: undefined,
    priority: 0,
  },
  {
    id: "content",
    name: "内容",
    priority: 1,
  },
  {
    id: "interface",
    name: "外观",
    priority: 2,
  },
  {
    id: "system",
    name: "系统",
    priority: 3,
  },
  {
    id: "tool",
    name: "工具",
    priority: 4,
  },
];

export default routes;
