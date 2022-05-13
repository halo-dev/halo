import type { RouteRecordRaw } from "vue-router";
import { BasicLayout, BlankLayout, SystemSettingsLayout } from "@/layouts";

import Dashboard from "../views/dashboard/Dashboard.vue";

import PostList from "../views/contents/posts/PostList.vue";
import PostEditor from "../views/contents/posts/PostEditor.vue";
import SheetList from "../views/contents/sheets/SheetList.vue";
import CategoryList from "../views/contents/posts/categories/CategoryList.vue";
import TagList from "../views/contents/posts/tags/TagList.vue";
import CommentList from "../views/contents/comments/CommentList.vue";
import AttachmentList from "../views/contents/attachments/AttachmentList.vue";

import ThemeList from "../views/interface/themes/ThemeList.vue";
import MenuList from "../views/interface/menus/MenuList.vue";
import Visual from "../views/interface/visual/Visual.vue";

import PluginList from "../views/system/plugins/PluginList.vue";
import UserList from "../views/system/users/UserList.vue";
import Profile from "../views/system/users/Profile.vue";
import GeneralSettings from "../views/system/settings/GeneralSettings.vue";
import NotificationSettings from "../views/system/settings/NotificationSettings.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    component: BasicLayout,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: Dashboard,
      },
    ],
  },
  {
    path: "/posts",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Posts",
        component: PostList,
      },
      {
        path: "editor",
        name: "PostEditor",
        component: PostEditor,
      },
      {
        path: "categories",
        component: BlankLayout,
        children: [
          {
            path: "",
            name: "Categories",
            component: CategoryList,
          },
        ],
      },
      {
        path: "tags",
        component: BlankLayout,
        children: [
          {
            path: "",
            name: "Tags",
            component: TagList,
          },
        ],
      },
    ],
  },
  {
    path: "/sheets",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Sheets",
        component: SheetList,
      },
    ],
  },
  {
    path: "/comments",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Comments",
        component: CommentList,
      },
    ],
  },
  {
    path: "/attachments",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Attachments",
        component: AttachmentList,
      },
    ],
  },
  {
    path: "/themes",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Themes",
        component: ThemeList,
      },
    ],
  },
  {
    path: "/menus",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Menus",
        component: MenuList,
      },
    ],
  },
  {
    path: "/visual",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Visual",
        component: Visual,
      },
    ],
  },
  {
    path: "/plugins",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Plugins",
        component: PluginList,
      },
    ],
  },
  {
    path: "/users",
    component: BasicLayout,
    redirect: "/users/profile",
    children: [
      {
        path: "",
        name: "Users",
        component: UserList,
      },
      {
        path: "profile",
        name: "Profile",
        component: Profile,
      },
    ],
  },
  {
    path: "/settings",
    component: SystemSettingsLayout,
    redirect: "/settings/general",
    children: [
      {
        path: "general",
        name: "GeneralSettings",
        component: GeneralSettings,
      },
      {
        path: "notification",
        name: "NotificationSettings",
        component: NotificationSettings,
      },
    ],
  },
];

export default routes;
