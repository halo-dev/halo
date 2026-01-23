import { IconNotificationBadgeLine } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import BasicLayout from "@uc/layouts/BasicLayout.vue";
import { markRaw } from "vue";

export default definePlugin({
  ucRoutes: [
    {
      path: "/notifications",
      name: "NotificationsRoot",
      component: BasicLayout,
      meta: {
        title: "core.uc_notification.title",
        searchable: true,
        hideFooter: true,
        menu: {
          name: "core.uc_sidebar.menu.items.notification",
          group: "dashboard",
          icon: markRaw(IconNotificationBadgeLine),
          priority: 1,
          mobile: true,
        },
      },
      children: [
        {
          path: "",
          name: "Notifications",
          component: () => import("./Notifications.vue"),
        },
      ],
    },
  ],
});
