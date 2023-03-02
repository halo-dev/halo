import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import Menus from "./Menus.vue";
import { IconListSettings } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
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
            menu: {
              name: "菜单",
              group: "interface",
              icon: markRaw(IconListSettings),
              priority: 1,
            },
          },
        },
      ],
    },
  ],
});
