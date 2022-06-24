import { BasicLayout, definePlugin } from "@halo-dev/admin-shared";
import MenuList from "./MenuList.vue";
import { IconListSettings } from "@halo-dev/components";

export default definePlugin({
  name: "menuModule",
  components: [],
  routes: [
    {
      path: "/menus",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Menus",
          component: MenuList,
        },
      ],
    },
  ],
  menus: [
    {
      name: "外观",
      items: [
        {
          name: "菜单",
          path: "/menus",
          icon: IconListSettings,
        },
      ],
    },
  ],
});
