import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import BlankLayout from "@/layouts/BlankLayout.vue";
import UserProfileLayout from "./layouts/UserProfileLayout.vue";
import UserStatsWidget from "./widgets/UserStatsWidget.vue";
import UserList from "./UserList.vue";
import UserDetail from "./UserDetail.vue";
import PersonalAccessTokens from "./PersonalAccessTokens.vue";
import Login from "./Login.vue";
import { IconUserSettings } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {
    UserStatsWidget,
  },
  routes: [
    {
      path: "/login",
      name: "Login",
      component: Login,
    },
    {
      path: "/users",
      component: BlankLayout,
      children: [
        {
          path: "",
          component: BasicLayout,
          children: [
            {
              path: "",
              name: "Users",
              component: UserList,
              meta: {
                title: "用户",
                searchable: true,
                permissions: ["system:users:view"],
                menu: {
                  name: "用户",
                  group: "system",
                  icon: markRaw(IconUserSettings),
                  priority: 1,
                  mobile: true,
                },
              },
            },
          ],
        },
        {
          path: ":name",
          component: UserProfileLayout,
          name: "User",
          children: [
            {
              path: "detail",
              name: "UserDetail",
              component: UserDetail,
              meta: {
                title: "用户详情",
              },
            },
            {
              path: "tokens",
              name: "PersonalAccessTokens",
              component: PersonalAccessTokens,
              meta: {
                title: "个人令牌",
              },
            },
          ],
        },
      ],
    },
  ],
});
