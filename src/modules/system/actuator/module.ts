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
            title: "系统概览",
            searchable: true,
            menu: {
              name: "概览",
              group: "system",
              icon: markRaw(IconTerminalBoxLine),
              priority: 3,
              mobile: true,
            },
          },
        },
      ],
    },
  ],
});
