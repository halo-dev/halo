import GatewayLayout from "@/layouts/GatewayLayout.vue";
import Forbidden from "@/views/exceptions/Forbidden.vue";
import NotFound from "@/views/exceptions/NotFound.vue";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import Binding from "@console/views/system/Binding.vue";
import Login from "@console/views/system/Login.vue";
import Redirect from "@console/views/system/Redirect.vue";
import ResetPassword from "@console/views/system/ResetPassword.vue";
import Setup from "@console/views/system/Setup.vue";
import SetupInitialData from "@console/views/system/SetupInitialData.vue";
import type { RouteRecordRaw } from "vue-router";

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
    path: "/login",
    component: GatewayLayout,
    children: [
      {
        path: "",
        name: "Login",
        component: Login,
        meta: {
          title: "core.login.title",
        },
      },
    ],
  },
  {
    path: "/binding/:provider",
    component: GatewayLayout,
    children: [
      {
        path: "",
        name: "Binding",
        component: Binding,
        meta: {
          title: "core.binding.title",
        },
      },
    ],
  },
  {
    path: "/setup",
    component: GatewayLayout,
    children: [
      {
        path: "",
        name: "Setup",
        component: Setup,
        meta: {
          title: "core.setup.title",
        },
      },
    ],
  },
  {
    path: "/setup-initial-data",
    name: "SetupInitialData",
    component: SetupInitialData,
    meta: {
      title: "core.setup.title",
    },
  },
  {
    path: "/redirect",
    name: "Redirect",
    component: Redirect,
  },
  {
    path: "/reset-password",
    component: GatewayLayout,
    children: [
      {
        path: "",
        name: "ResetPassword",
        component: ResetPassword,
        meta: {
          title: "core.reset_password.title",
        },
      },
    ],
  },
];

export default routes;
