import { BasicLayout, BlankLayout, definePlugin } from "@halo-dev/admin-shared";
import ThemeDetail from "./ThemeDetail.vue";
import Visual from "./Visual.vue";
import { IconPalette } from "@halo-dev/components";

export default definePlugin({
  name: "themeModule",
  components: [],
  routes: [
    {
      path: "/theme",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Theme",
          component: ThemeDetail,
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
