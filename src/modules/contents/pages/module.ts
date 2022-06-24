import { BasicLayout, definePlugin } from "@halo-dev/admin-shared";
import SheetList from "./PageList.vue";
import { IconPages } from "@halo-dev/components";

export default definePlugin({
  name: "pageModule",
  components: [],
  routes: [
    {
      path: "/pages",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Pages",
          component: SheetList,
        },
      ],
    },
  ],
  menus: [
    {
      name: "内容",
      items: [
        {
          name: "页面",
          path: "/pages",
          icon: IconPages,
        },
      ],
    },
  ],
});
