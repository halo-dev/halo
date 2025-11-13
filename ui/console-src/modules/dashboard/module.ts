import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconDashboard } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import WidgetCard from "./components/WidgetCard.vue";

export default definePlugin({
  components: {
    WidgetCard,
  },
  routes: [
    {
      path: "/",
      component: BasicLayout,
      name: "Root",
      redirect: "/dashboard",
      children: [
        {
          path: "dashboard",
          name: "Dashboard",
          component: () => import("./Dashboard.vue"),
          meta: {
            title: "core.dashboard.title",
            searchable: true,
            menu: {
              name: "core.sidebar.menu.items.dashboard",
              group: "dashboard",
              icon: markRaw(IconDashboard),
              priority: 0,
              mobile: true,
            },
          },
        },
        {
          path: "dashboard/designer",
          name: "DashboardDesigner",
          component: () => import("./DashboardDesigner.vue"),
          meta: {
            title: "core.dashboard_designer.title",
            searchable: false,
          },
        },
      ],
    },
  ],
});
