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
      redirect: "/settings/basic",
      children: [
        {
          path: ":group",
          name: "SystemSetting",
          component: SystemSetting,
          meta: {
            title: "系统设置",
          },
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
          path: "/settings",
          icon: IconSettings,
        },
      ],
    },
  ],
});
