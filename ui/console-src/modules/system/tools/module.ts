import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconToolsFill } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import Tools from "./Tools.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/tools",
      name: "ToolsRoot",
      component: BasicLayout,
      meta: {
        title: "core.tool.title",
        menu: {
          name: "core.sidebar.menu.items.tools",
          group: "system",
          icon: markRaw(IconToolsFill),
          priority: 5,
        },
      },
      children: [
        {
          path: "",
          name: "Tools",
          component: Tools,
        },
      ],
    },
  ],
});
