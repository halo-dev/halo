import type { RouteRecordRaw } from "vue-router";
import HomeView from "../views/HomeView.vue";
import AboutView from "../views/AboutView.vue";
import PostList from "../views/posts/PostList.vue";
import PageList from "../views/posts/PageList.vue";
import Profile from "../views/users/Profile.vue";
import Themes from "../views/interface/Themes.vue";
import Attachments from "../views/attachments/Attachments.vue";

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
    component: PageList,
  },
  {
    path: "/comment",
    name: "Comment",
    component: AboutView,
  },
  {
    path: "/attachment",
    name: "Attachment",
    component: Attachments,
  },
  {
    path: "/themes",
    name: "Themes",
    component: Themes,
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
    component: Profile,
  },
  {
    path: "/settings",
    name: "Settings",
    component: AboutView,
  },
];

export default routes;
