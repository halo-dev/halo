import { IconPalette } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/theme",
      name: "ThemeRoot",
      component: () => import("./layouts/ThemeLayout.vue"),
      meta: {
        title: "core.theme.title",
        searchable: true,
        permissions: ["system:themes:view"],
        menu: {
          name: "core.sidebar.menu.items.themes",
          group: "interface",
          icon: markRaw(IconPalette),
          priority: 0,
        },
      },
      children: [
        {
          path: "",
          name: "ThemeDetail",
          component: () => import("./ThemeDetail.vue"),
        },
        {
          path: "settings/:group",
          name: "ThemeSetting",
          component: () => import("./ThemeSetting.vue"),
          meta: {
            title: "core.theme.settings.title",
            permissions: ["system:themes:view"],
          },
        },
      ],
    },
  ],
});
