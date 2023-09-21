import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import Dashboard from "./Dashboard.vue";
import { IconDashboard } from "@halo-dev/components";

import QuickLinkWidget from "./widgets/QuickLinkWidget.vue";
import ViewsStatsWidget from "./widgets/ViewsStatsWidget.vue";
import NotificationWidget from "./widgets/NotificationWidget.vue";
import { markRaw } from "vue";

export default definePlugin({
  components: {
    QuickLinkWidget,
    ViewsStatsWidget,
    NotificationWidget,
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
          component: Dashboard,
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
      ],
    },
  ],
});
