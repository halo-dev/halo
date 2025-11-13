import BasicLayout from "@console/layouts/BasicLayout.vue";
import { definePlugin } from "@halo-dev/ui-shared";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/users/roles",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Roles",
          component: () => import("./RoleList.vue"),
          meta: {
            title: "core.role.title",
            searchable: true,
            permissions: ["system:roles:view"],
          },
        },
        {
          path: ":name",
          name: "RoleDetail",
          component: () => import("./RoleDetail.vue"),
          meta: {
            title: "core.role.detail.title",
            permissions: ["system:roles:view"],
          },
        },
      ],
    },
  ],
});
