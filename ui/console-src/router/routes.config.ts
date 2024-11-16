import Forbidden from "@/views/exceptions/Forbidden.vue";
import NotFound from "@/views/exceptions/NotFound.vue";
import BasicLayout from "@console/layouts/BasicLayout.vue";
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
];

export default routes;
