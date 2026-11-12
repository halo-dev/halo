import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconUserSettings } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";

export default definePlugin({
  routes: [
    {
      path: "/users",
      name: "UsersRoot",
      component: BasicLayout,
      meta: {
        title: "core.user.title",
        searchable: true,
        permissions: ["system:users:view"],
        menu: {
          name: /* @x18n-key */ "core.sidebar.menu.items.users",
          group: "system",
          icon: markRaw(IconUserSettings),
          priority: 1,
          mobile: true,
        },
      },
      children: [
        {
          path: "",
          name: "Users",
          component: () => import("./UserList.vue"),
        },
        {
          path: ":name",
          name: "UserDetail",
          component: () => import("./UserDetail.vue"),
          meta: {
            title: /* @x18n-key */ "core.user.detail.title",
          },
        },
      ],
    },
  ],
});
