import type { RouteRecordRaw } from "vue-router";
import NotFound from "@/views/exceptions/NotFound.vue";
import Forbidden from "@/views/exceptions/Forbidden.vue";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import Setup from "@console/views/system/Setup.vue";
import Redirect from "@console/views/system/Redirect.vue";
import SetupInitialData from "@console/views/system/SetupInitialData.vue";

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
    name: "Setup",
    component: Setup,
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
  {
    path: "/redirect",
    name: "Redirect",
    component: Redirect,
  },
];

export default routes;
