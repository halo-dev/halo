import type { RouteRecordRaw } from "vue-router";
import NotFound from "@/views/exceptions/NotFound.vue";
import { BasicLayout } from "@halo-dev/admin-shared";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/:pathMatch(.*)*",
    component: BasicLayout,
    children: [{ path: "", name: "NotFound", component: NotFound }],
  },
];

export default routes;
