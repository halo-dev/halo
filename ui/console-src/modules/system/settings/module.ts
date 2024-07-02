import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconSettings } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import SystemSettings from "./SystemSettings.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/settings",
      name: "SettingsRoot",
      component: BasicLayout,
      meta: {
        title: "core.setting.title",
        permissions: ["system:settings:view"],
        menu: {
          name: "core.sidebar.menu.items.settings",
          group: "system",
          icon: markRaw(IconSettings),
          priority: 2,
        },
      },
      children: [
        {
          path: "",
          name: "SystemSetting",
          component: SystemSettings,
        },
      ],
    },
  ],
});
