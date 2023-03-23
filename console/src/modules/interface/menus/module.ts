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
            title: "core.menu.title",
            searchable: true,
            permissions: ["system:menus:view"],
            menu: {
              name: "core.sidebar.menu.items.menus",
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
