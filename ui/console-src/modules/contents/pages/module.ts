import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconPages } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import SinglePageList from "./SinglePageList.vue";

export default definePlugin({
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
          component: () => import("./DeletedSinglePageList.vue"),
          meta: {
            title: "core.deleted_page.title",
            searchable: true,
            permissions: ["system:singlepages:view"],
          },
        },
        {
          path: "editor",
          name: "SinglePageEditor",
          component: () => import("./SinglePageEditor.vue"),
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
          component: () => import("./SinglePageSnapshots.vue"),
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
