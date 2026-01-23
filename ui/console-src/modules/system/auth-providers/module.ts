import BasicLayout from "@console/layouts/BasicLayout.vue";
import { definePlugin } from "@halo-dev/ui-shared";

export default definePlugin({
  routes: [
    {
      path: "/users/auth-providers",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "AuthProviders",
          component: () => import("./AuthProviders.vue"),
          meta: {
            title: "core.identity_authentication.title",
            searchable: true,
            permissions: ["*"],
          },
        },
        {
          path: ":name",
          name: "AuthProviderDetail",
          component: () => import("./AuthProviderDetail.vue"),
          meta: {
            title: "core.identity_authentication.detail.title",
            permissions: ["*"],
          },
        },
      ],
    },
  ],
});
