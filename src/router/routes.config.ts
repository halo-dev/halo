import type { RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import AboutView from "../views/AboutView.vue";
import PostList from "../views/posts/PostList.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "Dashboard",
    component: HomeView,
  },
  {
    path: "/about",
    name: "about",
    component: AboutView,
  },
  {
    path: "/posts",
    name: "Posts",
    component: PostList,
    children: [
      {
        path: "/posts/categories",
        name: "Categories",
        component: AboutView,
      },
      {
        path: "/posts/tags",
        name: "Tags",
        component: AboutView,
      },
    ],
  },
  {
    path: "/sheets",
    name: "Sheets",
    component: AboutView,
  },
  {
    path: "/comment",
    name: "Comment",
    component: AboutView,
  },
  {
    path: "/attachment",
    name: "Attachment",
    component: AboutView,
  },
  {
    path: "/themes",
    name: "Themes",
    component: AboutView,
  },
  {
    path: "/menus",
    name: "Menus",
    component: AboutView,
  },
  {
    path: "/visual",
    name: "Visual",
    component: AboutView,
  },
  {
    path: "/plugins",
    name: "Plugins",
    component: AboutView,
  },
  {
    path: "/users",
    name: "Users",
    component: AboutView,
  },
  {
    path: "/settings",
    name: "Settings",
    component: AboutView,
  },
];

export default routes;
