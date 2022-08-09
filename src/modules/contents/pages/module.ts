import { BasicLayout, definePlugin } from "@halo-dev/admin-shared";
import PageList from "./PageList.vue";
import { IconPages } from "@halo-dev/components";

export default definePlugin({
  name: "pageModule",
  components: [],
  routes: [
    {
      path: "/pages",
      component: BasicLayout,
      name: "BasePages",
      children: [
        {
          path: "",
          name: "Pages",
          component: PageList,
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
