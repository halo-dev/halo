import { BlankLayout, definePlugin } from "@halo-dev/admin-shared";
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
        },
        {
          path: "settings/:group",
          name: "ThemeSetting",
          component: ThemeSetting,
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
