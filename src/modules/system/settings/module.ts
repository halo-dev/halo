import { definePlugin } from "@halo-dev/admin-shared";
import SystemSettingsLayout from "./layouts/SystemSettingsLayout.vue";
import SystemSetting from "./SystemSetting.vue";
import { IconSettings } from "@halo-dev/components";

export default definePlugin({
  name: "settingModule",
  components: [],
  routes: [
    {
      path: "/settings",
      component: SystemSettingsLayout,
      children: [
        {
          path: ":group",
          name: "SystemSetting",
          component: SystemSetting,
        },
      ],
    },
  ],
  menus: [
    {
      name: "系统",
      items: [
        {
          name: "设置",
          path: "/settings/basic",
          icon: IconSettings,
        },
      ],
    },
  ],
});
