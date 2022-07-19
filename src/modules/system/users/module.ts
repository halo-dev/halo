import {
  BasicLayout,
  BlankLayout,
  definePlugin,
  UserProfileLayout,
} from "@halo-dev/admin-shared";
import UserList from "./UserList.vue";
import UserDetail from "./UserDetail.vue";
import ProfileModification from "./ProfileModification.vue";
import PasswordChange from "./PasswordChange.vue";
import PersonalAccessTokens from "./PersonalAccessTokens.vue";
import Login from "./Login.vue";
import { IconUserSettings } from "@halo-dev/components";

export default definePlugin({
  name: "userModule",
  components: [],
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
                permissions: ["system:users:view"],
              },
            },
          ],
        },
        {
          path: ":name",
          component: UserProfileLayout,
          children: [
            {
              path: "detail",
              name: "UserDetail",
              component: UserDetail,
            },
            {
              path: "profile-modification",
              name: "ProfileModification",
              component: ProfileModification,
            },
            {
              path: "password-change",
              name: "PasswordChange",
              component: PasswordChange,
            },
            {
              path: "tokens",
              name: "PersonalAccessTokens",
              component: PersonalAccessTokens,
            },
          ],
        },
      ],
    },
  ],
  menus: [
    {
      name: "系统",
      items: [
        {
          name: "用户",
          path: "/users",
          icon: IconUserSettings,
        },
      ],
    },
  ],
});
