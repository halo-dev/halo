import BasicLayout from "@console/layouts/BasicLayout.vue";
import { definePlugin } from "@halo-dev/console-shared";
import { IconMotionLine } from "@halo-dev/components";
import { markRaw } from "vue";
import Tools from "./Tools.vue";
export default definePlugin({
  routes: [
    {
      path: "/tools",
      name: "ToolsRoot",
      component: BasicLayout,
      meta: {
        title: "工具",
        menu: {
          name: "工具",
          group: "system",
          icon: markRaw(IconMotionLine),
          priority: 5,
          mobile: true,
        },
      },
      children: [
        {
          path: "",
          name: "Tools",
          component: Tools,
          meta: {
            title: "工具",
          },
        },
      ],
    },
  ],
});
