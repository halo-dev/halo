import { IconPalette } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import ThemeDetail from "./ThemeDetail.vue";
import ThemeSetting from "./ThemeSetting.vue";
import ThemeLayout from "./layouts/ThemeLayout.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/theme",
      name: "ThemeRoot",
      component: ThemeLayout,
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
          component: ThemeDetail,
        },
        {
          path: "settings/:group",
          name: "ThemeSetting",
          component: ThemeSetting,
          meta: {
            title: "core.theme.settings.title",
            permissions: ["system:themes:view"],
          },
        },
      ],
    },
  ],
});
