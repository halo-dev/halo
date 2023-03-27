import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import SinglePageList from "./SinglePageList.vue";
import DeletedSinglePageList from "./DeletedSinglePageList.vue";
import SinglePageEditor from "./SinglePageEditor.vue";
import SinglePageStatsWidget from "./widgets/SinglePageStatsWidget.vue";
import { IconPages } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {
    SinglePageStatsWidget,
  },
  routes: [
    {
      path: "/single-pages",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "SinglePages",
          component: SinglePageList,
          meta: {
            title: "core.page.title",
            searchable: true,
            permissions: ["system:singlepages:view"],
            menu: {
              name: "core.sidebar.menu.items.single_pages",
              group: "content",
              icon: markRaw(IconPages),
              priority: 1,
            },
          },
        },
        {
          path: "deleted",
          name: "DeletedSinglePages",
          component: DeletedSinglePageList,
          meta: {
            title: "core.deleted_page.title",
            searchable: true,
            permissions: ["system:singlepages:view"],
          },
        },
        {
          path: "editor",
          name: "SinglePageEditor",
          component: SinglePageEditor,
          meta: {
            title: "core.page_editor.title",
            searchable: true,
            permissions: ["system:singlepages:manage"],
          },
        },
      ],
    },
  ],
});
