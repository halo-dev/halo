import type { Plugin } from "@halo-dev/admin-shared";
import { BasicLayout } from "@/layouts";
import RoleList from "./RoleList.vue";
import RoleDetail from "./RoleDetail.vue";

const roleModule: Plugin = {
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
        },
        {
          path: "roles/:id",
          name: "RoleDetail",
          component: RoleDetail,
        },
      ],
    },
  ],
  menus: [],
};

export default roleModule;
