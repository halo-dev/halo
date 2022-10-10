import { BlankLayout, definePlugin } from "@halo-dev/console-shared";
import ThemeLayout from "./layouts/ThemeLayout.vue";
import ThemeDetail from "./ThemeDetail.vue";
import ThemeSetting from "./ThemeSetting.vue";
import Visual from "./Visual.vue";
import { IconPalette } from "@halo-dev/components";

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
  menus: [
    {
      name: "外观",
      items: [
        {
          name: "主题",
          path: "/theme",
          icon: IconPalette,
        },
      ],
    },
  ],
});
