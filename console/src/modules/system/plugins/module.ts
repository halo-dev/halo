import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import BlankLayout from "@/layouts/BlankLayout.vue";
import PluginList from "./PluginList.vue";
import PluginDetail from "./PluginDetail.vue";
import { IconPlug } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/plugins",
      component: BlankLayout,
      children: [
        {
          path: "",
          component: BasicLayout,
          children: [
            {
              path: "",
              name: "Plugins",
              component: PluginList,
              meta: {
                title: "core.plugin.title",
                searchable: true,
                permissions: ["system:plugins:view"],
                menu: {
                  name: "core.sidebar.menu.items.plugins",
                  group: "system",
                  icon: markRaw(IconPlug),
                  priority: 0,
                },
              },
            },
          ],
        },
        {
          path: ":name",
          component: BasicLayout,
          children: [
            {
              path: "",
              name: "PluginDetail",
              component: PluginDetail,
              meta: {
                title: "core.plugin.detail.title",
                permissions: ["system:plugins:view"],
              },
            },
          ],
        },
      ],
    },
  ],
});
