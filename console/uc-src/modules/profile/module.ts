import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@uc/layouts/BasicLayout.vue";
import { IconAccountCircleLine } from "@halo-dev/components";
import { markRaw } from "vue";
import Profile from "./Profile.vue";

export default definePlugin({
  ucRoutes: [
    {
      path: "/",
      component: BasicLayout,
      name: "Root",
      redirect: "/profile",
      children: [
        {
          path: "profile",
          name: "Profile",
          component: Profile,
          meta: {
            title: "个人资料",
            searchable: true,
            menu: {
              name: "我的",
              group: "dashboard",
              icon: markRaw(IconAccountCircleLine),
              priority: 0,
              mobile: true,
            },
          },
        },
      ],
    },
  ],
});
