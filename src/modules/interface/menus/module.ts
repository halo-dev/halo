import type { Plugin } from "@halo-dev/admin-shared";
import { BasicLayout } from "@/layouts";
import MenuList from "./MenuList.vue";
import { IconListSettings } from "@halo-dev/components";

const menuModule: Plugin = {
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
};

export default menuModule;
