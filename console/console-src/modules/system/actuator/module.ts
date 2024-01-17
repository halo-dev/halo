import { definePlugin } from "@halo-dev/console-shared";
import { IconTerminalBoxLine } from "@halo-dev/components";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import Actuator from "./Actuator.vue";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/actuator",
      name: "OverviewRoot", // fixme: actuator will be renamed to overview in the future
      component: BasicLayout,
      meta: {
        title: "core.actuator.title",
        searchable: true,
        permissions: ["system:actuator:manage"],
        menu: {
          name: "core.sidebar.menu.items.actuator",
          group: "system",
          icon: markRaw(IconTerminalBoxLine),
          priority: 3,
        },
      },
      children: [
        {
          path: "",
          name: "Actuator",
          component: Actuator,
        },
      ],
    },
  ],
});
