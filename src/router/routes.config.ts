import type { RouteRecordRaw } from "vue-router";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: () => import("../views/HomeView.vue"),
    children: [
      {
        path: "/about",
        name: "about",
        component: () => import("../views/AboutView.vue"),
      },
    ],
  },
];

export default routes;
