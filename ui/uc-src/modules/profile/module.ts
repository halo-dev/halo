import { IconAccountCircleLine } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@uc/layouts/BasicLayout.vue";
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
            title: "core.uc_profile.title",
            searchable: true,
            menu: {
              name: "core.uc_sidebar.menu.items.profile",
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
