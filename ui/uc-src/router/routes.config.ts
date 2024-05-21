import type { RouteRecordRaw } from "vue-router";
import NotFound from "@/views/exceptions/NotFound.vue";
import Forbidden from "@/views/exceptions/Forbidden.vue";
import BasicLayout from "@uc/layouts/BasicLayout.vue";
import ResetPassword from "@uc/views/ResetPassword.vue";
import GatewayLayout from "@/layouts/GatewayLayout.vue";

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
    path: "/reset-password/:username",
    component: GatewayLayout,
    children: [
      {
        path: "",
        name: "ResetPassword",
        component: ResetPassword,
        meta: {
          title: "core.uc_reset_password.title",
        },
      },
    ],
  },
];

export default routes;
