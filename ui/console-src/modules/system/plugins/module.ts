import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconPlug } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import type { RouteRecordRaw } from "vue-router";
import PluginDetailModal from "./components/PluginDetailModal.vue";

declare module "vue" {
  interface GlobalComponents {
    PluginDetailModal: (typeof import("./components/PluginDetailModal.vue"))["default"];
  }
}

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
          component: () => import("./PluginList.vue"),
        },
        {
          path: "extension-point-settings",
          name: "PluginExtensionPointSettings",
          component: () => import("./PluginExtensionPointSettings.vue"),
          meta: {
            title: "core.plugin.extension-settings.title",
            hideFooter: true,
            permissions: ["*"],
          },
        },
        {
          path: ":name",
          name: "PluginDetail",
          component: () => import("./PluginDetail.vue"),
          meta: {
            title: "core.plugin.detail.title",
            permissions: ["system:plugins:view"],
          },
        },
      ],
    } as RouteRecordRaw,
  ],
});
