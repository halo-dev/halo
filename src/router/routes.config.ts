import type { RouteRecordRaw } from "vue-router";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "Dashboard",
    component: () => import("../views/HomeView.vue"),
  },
  {
    path: "/about",
    name: "about",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/posts",
    name: "Posts",
    component: () => import("../views/AboutView.vue"),
    children: [
      {
        path: "/posts/categories",
        name: "Categories",
        component: () => import("../views/AboutView.vue"),
      },
      {
        path: "/posts/tags",
        name: "Tags",
        component: () => import("../views/AboutView.vue"),
      },
    ],
  },
  {
    path: "/sheets",
    name: "Sheets",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/comment",
    name: "Comment",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/attachment",
    name: "Attachment",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/themes",
    name: "Themes",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/menus",
    name: "Menus",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/visual",
    name: "Visual",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/plugins",
    name: "Plugins",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/users",
    name: "Users",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/settings",
    name: "Settings",
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/components",
    name: "Components",
    component: () => import("../views/ViewComponents.vue"),
  },
];

export default routes;
