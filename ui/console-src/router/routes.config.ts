import BasicLayout from "@console/layouts/BasicLayout.vue";
import type { RouteRecordRaw } from "vue-router";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/:pathMatch(.*)*",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "NotFound",
        component: () => import("@/views/exceptions/NotFound.vue"),
      },
    ],
  },
  {
    path: "/403",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Forbidden",
        component: () => import("@/views/exceptions/Forbidden.vue"),
      },
    ],
  },
];

export default routes;
