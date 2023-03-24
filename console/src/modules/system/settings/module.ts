import { definePlugin } from "@halo-dev/console-shared";
import SystemSettingsLayout from "./layouts/SystemSettingsLayout.vue";
import SystemSetting from "./SystemSetting.vue";
import { IconSettings } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/settings",
      component: SystemSettingsLayout,
      redirect: "/settings/basic",
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
          path: ":group",
          name: "SystemSetting",
          component: SystemSetting,
        },
      ],
    },
  ],
});
