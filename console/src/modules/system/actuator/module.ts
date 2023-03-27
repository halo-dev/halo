import { definePlugin } from "@halo-dev/console-shared";
import { IconTerminalBoxLine } from "@halo-dev/components";
import BasicLayout from "@/layouts/BasicLayout.vue";
import Actuator from "./Actuator.vue";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/actuator",
      component: BasicLayout,
      children: [
        {
          path: "",
          component: Actuator,
          meta: {
            title: "core.actuator.title",
            searchable: true,
            menu: {
              name: "core.sidebar.menu.items.actuator",
              group: "system",
              icon: markRaw(IconTerminalBoxLine),
              priority: 3,
            },
          },
        },
      ],
    },
  ],
});
