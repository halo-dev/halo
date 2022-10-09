import { BasicLayout, definePlugin } from "@halo-dev/console-shared";
import Menus from "./Menus.vue";
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
          component: Menus,
          meta: {
            title: "菜单",
            searchable: true,
            permissions: ["system:menus:view"],
          },
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
