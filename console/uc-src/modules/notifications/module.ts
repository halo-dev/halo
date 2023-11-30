import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@uc/layouts/BasicLayout.vue";
import { IconNotificationBadgeLine } from "@halo-dev/components";
import { markRaw } from "vue";
import Notifications from "./Notifications.vue";

export default definePlugin({
  ucRoutes: [
    {
      path: "/notifications",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Notifications",
          component: Notifications,
          meta: {
            title: "消息",
            searchable: true,
            menu: {
              name: "消息",
              group: "dashboard",
              icon: markRaw(IconNotificationBadgeLine),
              priority: 1,
              mobile: true,
            },
          },
        },
      ],
    },
  ],
});
