import { definePlugin, SystemSettingsLayout } from "@halo-dev/admin-shared";
import GeneralSettings from "./GeneralSettings.vue";
import NotificationSettings from "./NotificationSettings.vue";
import { IconSettings } from "@halo-dev/components";

export default definePlugin({
  name: "settingModule",
  components: [],
  routes: [
    {
      path: "/settings",
      component: SystemSettingsLayout,
      redirect: "/settings/general",
      children: [
        {
          path: "general",
          name: "GeneralSettings",
          component: GeneralSettings,
        },
        {
          path: "notification",
          name: "NotificationSettings",
          component: NotificationSettings,
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
