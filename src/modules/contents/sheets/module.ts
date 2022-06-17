import { definePlugin } from "@halo-dev/admin-shared";
import { BasicLayout } from "@/layouts";
import SheetList from "./SheetList.vue";
import { IconPages } from "@halo-dev/components";

export default definePlugin({
  name: "sheetModule",
  components: [],
  routes: [
    {
      path: "/sheets",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Sheets",
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
          path: "/sheets",
          icon: IconPages,
        },
      ],
    },
  ],
});
