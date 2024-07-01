import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconPlug } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import type { RouteRecordRaw } from "vue-router";
import PluginDetail from "./PluginDetail.vue";
import PluginExtensionPointSettings from "./PluginExtensionPointSettings.vue";
import PluginList from "./PluginList.vue";
import PluginDetailModal from "./components/PluginDetailModal.vue";

export default definePlugin({
  components: {
    PluginDetailModal,
  },
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
          path: "extension-point-settings",
          name: "PluginExtensionPointSettings",
          component: PluginExtensionPointSettings,
          meta: {
            title: "core.plugin.extension-settings.title",
            hideFooter: true,
            permissions: ["*"],
          },
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
    } as RouteRecordRaw,
  ],
});
