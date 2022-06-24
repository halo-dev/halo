import { BasicLayout, definePlugin } from "@halo-dev/admin-shared";
import PluginList from "./PluginList.vue";
import PluginDetail from "./PluginDetail.vue";
import { IconPlug } from "@halo-dev/components";

export default definePlugin({
  name: "pluginModule",
  components: [],
  routes: [
    {
      path: "/plugins",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Plugins",
          component: PluginList,
        },
        {
          path: ":pluginName",
          name: "PluginDetail",
          component: PluginDetail,
        },
      ],
    },
  ],
  menus: [
    {
      name: "系统",
      items: [
        {
          name: "插件",
          path: "/plugins",
          icon: IconPlug,
        },
      ],
    },
  ],
});
