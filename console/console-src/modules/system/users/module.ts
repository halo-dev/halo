import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import UserStatsWidget from "./widgets/UserStatsWidget.vue";
import UserList from "./UserList.vue";
import UserDetail from "./UserDetail.vue";
import Login from "./Login.vue";
import { IconUserSettings } from "@halo-dev/components";
import { markRaw } from "vue";
import Binding from "./Binding.vue";
import NotificationWidget from "./widgets/NotificationWidget.vue";

export default definePlugin({
  components: {
    UserStatsWidget,
    NotificationWidget,
  },
  routes: [
    {
      path: "/login",
      name: "Login",
      component: Login,
      meta: {
        title: "core.login.title",
      },
    },
    {
      path: "/binding/:provider",
      name: "Binding",
      component: Binding,
      meta: {
        title: "core.binding.title",
      },
    },
    {
      path: "/users",
      name: "UsersRoot",
      component: BasicLayout,
      meta: {
        title: "core.user.title",
        searchable: true,
        permissions: ["system:users:view"],
        menu: {
          name: "core.sidebar.menu.items.users",
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
          component: UserList,
        },
        {
          path: ":name",
          name: "UserDetail",
          component: UserDetail,
          meta: {
            title: "core.user.detail.title",
          },
        },
      ],
    },
  ],
});
