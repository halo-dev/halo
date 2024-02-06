import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import PluginList from "./PluginList.vue";
import PluginDetail from "./PluginDetail.vue";
import { IconPlug } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/plugins",
      name: "PluginsRoot",
      component: BasicLayout,
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
      children: [
        {
          path: "",
          name: "Plugins",
          component: PluginList,
        },
        {
          path: ":name",
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
});
