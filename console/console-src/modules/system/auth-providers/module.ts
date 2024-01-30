import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import AuthProviders from "./AuthProviders.vue";
import AuthProviderDetail from "./AuthProviderDetail.vue";

export default definePlugin({
  routes: [
    {
      path: "/users/auth-providers",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "AuthProviders",
          component: AuthProviders,
          meta: {
            title: "core.identity_authentication.title",
            searchable: true,
          },
        },
        {
          path: ":name",
          name: "AuthProviderDetail",
          component: AuthProviderDetail,
          meta: {
            title: "core.identity_authentication.detail.title",
          },
        },
      ],
    },
  ],
});
