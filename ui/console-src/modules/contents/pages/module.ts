import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import SinglePageList from "./SinglePageList.vue";
import DeletedSinglePageList from "./DeletedSinglePageList.vue";
import SinglePageEditor from "./SinglePageEditor.vue";
import SinglePageStatsWidget from "./widgets/SinglePageStatsWidget.vue";
import { IconPages } from "@halo-dev/components";
import { markRaw } from "vue";
import SinglePageSnapshots from "./SinglePageSnapshots.vue";

export default definePlugin({
  components: {
    SinglePageStatsWidget,
  },
  routes: [
    {
      path: "/single-pages",
      name: "SinglePagesRoot",
      component: BasicLayout,
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
      children: [
        {
          path: "",
          name: "SinglePages",
          component: SinglePageList,
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
            hideFooter: true,
            permissions: ["system:singlepages:manage"],
          },
        },
        {
          path: "snapshots",
          name: "SinglePageSnapshots",
          component: SinglePageSnapshots,
          meta: {
            title: "core.page_snapshots.title",
            searchable: false,
            hideFooter: true,
            permissions: ["system:singlepages:manage"],
          },
        },
      ],
    },
  ],
});
