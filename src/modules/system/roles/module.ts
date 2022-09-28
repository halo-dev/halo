import { BasicLayout, definePlugin } from "@halo-dev/admin-shared";
import RoleList from "./RoleList.vue";
import RoleDetail from "./RoleDetail.vue";

export default definePlugin({
  name: "roleModule",
  components: [],
  routes: [
    {
      path: "/users",
      component: BasicLayout,
      children: [
        {
          path: "roles",
          name: "Roles",
          component: RoleList,
          meta: {
            title: "角色",
            searchable: true,
            permissions: ["system:roles:view"],
          },
        },
        {
          path: "roles/:name",
          name: "RoleDetail",
          component: RoleDetail,
          meta: {
            title: "角色详情",
            permissions: ["system:roles:view"],
          },
        },
      ],
    },
  ],
  menus: [],
});
