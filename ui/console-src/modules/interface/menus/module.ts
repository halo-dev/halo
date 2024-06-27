import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconListSettings } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import Menus from "./Menus.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/menus",
      name: "MenusRoot",
      component: BasicLayout,
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
      children: [
        {
          path: "",
          name: "Menus",
          component: Menus,
        },
      ],
    },
  ],
});
