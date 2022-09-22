import { BasicLayout, BlankLayout, definePlugin } from "@halo-dev/admin-shared";
import PageLayout from "./layouts/PageLayout.vue";
import FunctionalPageList from "./FunctionalPageList.vue";
import SinglePageList from "./SinglePageList.vue";
import SinglePageEditor from "./SinglePageEditor.vue";
import { IconPages } from "@halo-dev/components";

export default definePlugin({
  name: "pageModule",
  components: [],
  routes: [
    {
      path: "/pages",
      component: BlankLayout,
      name: "BasePages",
      redirect: {
        name: "FunctionalPages",
      },
      children: [
        {
          path: "functional",
          component: PageLayout,
          children: [
            {
              path: "",
              name: "FunctionalPages",
              component: FunctionalPageList,
              meta: {
                title: "功能页面",
              },
            },
          ],
        },
        {
          path: "single",
          component: BlankLayout,
          children: [
            {
              path: "",
              component: PageLayout,
              children: [
                {
                  path: "",
                  name: "SinglePages",
                  component: SinglePageList,
                  meta: {
                    title: "自定义页面",
                  },
                },
              ],
            },
            {
              path: "editor",
              component: BasicLayout,
              children: [
                {
                  path: "",
                  name: "SinglePageEditor",
                  component: SinglePageEditor,
                  meta: {
                    title: "页面编辑",
                  },
                },
              ],
            },
          ],
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
