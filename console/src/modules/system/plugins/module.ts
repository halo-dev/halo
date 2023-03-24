import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import BlankLayout from "@/layouts/BlankLayout.vue";
import PluginLayout from "./layouts/PluginLayout.vue";
import PluginList from "./PluginList.vue";
import PluginSetting from "./PluginSetting.vue";
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
          component: PluginLayout,
          children: [
            {
              path: "detail",
              name: "PluginDetail",
              component: PluginDetail,
              meta: {
                title: "core.plugin.detail.title",
                permissions: ["system:plugins:view"],
              },
            },
            {
              path: "settings/:group",
              name: "PluginSetting",
              component: PluginSetting,
              meta: {
                title: "core.plugin.settings.title",
                permissions: ["system:plugins:manage"],
              },
            },
          ],
        },
      ],
    },
  ],
});
