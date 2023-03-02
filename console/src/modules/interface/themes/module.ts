import { definePlugin } from "@halo-dev/console-shared";
import ThemeLayout from "./layouts/ThemeLayout.vue";
import ThemeDetail from "./ThemeDetail.vue";
import ThemeSetting from "./ThemeSetting.vue";
import { IconPalette } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      path: "/theme",
      component: ThemeLayout,
      children: [
        {
          path: "",
          name: "ThemeDetail",
          component: ThemeDetail,
          meta: {
            title: "主题",
            searchable: true,
            permissions: ["system:themes:view"],
            menu: {
              name: "主题",
              group: "interface",
              icon: markRaw(IconPalette),
              priority: 0,
            },
          },
        },
        {
          path: "settings/:group",
          name: "ThemeSetting",
          component: ThemeSetting,
          meta: {
            title: "主题设置",
            permissions: ["system:themes:view"],
          },
        },
      ],
    },
  ],
});
