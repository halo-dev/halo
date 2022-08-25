import { definePlugin } from "@halo-dev/admin-shared";
import SystemSettingsLayout from "./layouts/SystemSettingsLayout.vue";
import GeneralSettings from "./GeneralSettings.vue";
import UserSettings from "./UserSettings.vue";
import PostSettings from "./PostSettings.vue";
import SeoSettings from "./SeoSettings.vue";
import CommentSettings from "./CommentSettings.vue";
import CodeInjectSettings from "./CodeInjectSettings.vue";
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
          path: "user",
          name: "UserSettings",
          component: UserSettings,
        },
        {
          path: "post",
          name: "PostSettings",
          component: PostSettings,
        },
        {
          path: "seo",
          name: "SeoSettings",
          component: SeoSettings,
        },
        {
          path: "comment",
          name: "CommentSettings",
          component: CommentSettings,
        },
        {
          path: "code-inject",
          name: "CodeInjectSettings",
          component: CodeInjectSettings,
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
