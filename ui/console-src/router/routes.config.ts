import Forbidden from "@/views/exceptions/Forbidden.vue";
import NotFound from "@/views/exceptions/NotFound.vue";
import BasicLayout from "@console/layouts/BasicLayout.vue";
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
    path: "/setup",
    component: Setup,
    name: "Setup",
    meta: {
      title: "core.setup.title",
    },
  },
  {
    path: "/setup-initial-data",
    name: "SetupInitialData",
    component: SetupInitialData,
    meta: {
      title: "core.setup.title",
    },
  },
];

export default routes;
