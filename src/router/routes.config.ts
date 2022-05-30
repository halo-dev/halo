import type { RouteRecordRaw } from "vue-router";
import {
  BasicLayout,
  BlankLayout,
  SystemSettingsLayout,
  UserProfileLayout,
} from "@/layouts";

import Dashboard from "../views/dashboard/Dashboard.vue";

import PostList from "../views/contents/posts/PostList.vue";
import PostEditor from "../views/contents/posts/PostEditor.vue";
import SheetList from "../views/contents/sheets/SheetList.vue";
import CategoryList from "../views/contents/posts/categories/CategoryList.vue";
import TagList from "../views/contents/posts/tags/TagList.vue";
import CommentList from "../views/contents/comments/CommentList.vue";
import AttachmentList from "../views/contents/attachments/AttachmentList.vue";

import ThemeDetail from "../views/interface/themes/ThemeDetail.vue";
import MenuList from "../views/interface/menus/MenuList.vue";
import Visual from "../views/interface/themes/Visual.vue";

import PluginList from "../views/system/plugins/PluginList.vue";
import PluginDetail from "../views/system/plugins/PluginDetail.vue";
import UserList from "../views/system/users/UserList.vue";
import RoleList from "../views/system/roles/RoleList.vue";
import RoleDetail from "../views/system/roles/RoleDetail.vue";
import UserDetail from "../views/system/users/UserDetail.vue";
import ProfileModification from "../views/system/users/ProfileModification.vue";
import PasswordChange from "../views/system/users/PasswordChange.vue";
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
    path: "/theme",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Theme",
        component: ThemeDetail,
      },
    ],
  },
  {
    path: "/theme/visual",
    component: BlankLayout,
    children: [
      {
        path: "",
        name: "ThemeVisual",
        component: Visual,
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
    path: "/plugins",
    component: BasicLayout,
    children: [
      {
        path: "",
        name: "Plugins",
        component: PluginList,
      },
      {
        path: ":id",
        name: "PluginDetail",
        component: PluginDetail,
      },
    ],
  },
  {
    path: "/users",
    component: BlankLayout,
    children: [
      {
        path: "",
        component: BasicLayout,
        children: [
          {
            path: "",
            name: "Users",
            component: UserList,
          },
        ],
      },
      {
        path: ":username",
        component: UserProfileLayout,
        alias: ["profile"],
        children: [
          {
            path: "detail",
            name: "UserDetail",
            component: UserDetail,
          },
          {
            path: "profile-modification",
            name: "ProfileModification",
            component: ProfileModification,
          },
          {
            path: "password-change",
            name: "PasswordChange",
            component: PasswordChange,
          },
        ],
      },
      {
        path: "",
        component: BasicLayout,
        children: [
          {
            path: "roles",
            name: "Roles",
            component: RoleList,
          },
          {
            path: "roles/:id",
            name: "RoleDetail",
            component: RoleDetail,
          },
        ],
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
