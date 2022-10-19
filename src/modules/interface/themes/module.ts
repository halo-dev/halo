import { definePlugin } from "@halo-dev/console-shared";
import BlankLayout from "@/layouts/BlankLayout.vue";
import ThemeLayout from "./layouts/ThemeLayout.vue";
import ThemeDetail from "./ThemeDetail.vue";
import ThemeSetting from "./ThemeSetting.vue";
import Visual from "./Visual.vue";
import { IconPalette } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  name: "themeModule",
  components: [],
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
            permissions: ["system:settings:view"],
          },
        },
      ],
    },
    {
      path: "/theme/visual",
      component: BlankLayout,
      children: [
        {
          path: "",
          name: "ThemeVisual",
          component: Visual,
          meta: {
            title: "可视化编辑",
          },
        },
      ],
    },
  ],
});
