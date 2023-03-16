import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import AuthProviders from "./AuthProviders.vue";
import AuthProviderDetail from "./AuthProviderDetail.vue";

export default definePlugin({
  routes: [
    {
      path: "/users",
      component: BasicLayout,
      children: [
        {
          path: "auth-providers",
          name: "AuthProviders",
          component: AuthProviders,
          meta: {
            title: "认证方式",
            searchable: true,
          },
        },
        {
          path: "auth-providers/:name",
          name: "AuthProviderDetail",
          component: AuthProviderDetail,
          meta: {
            title: "认证方式详情",
          },
        },
      ],
    },
  ],
});
