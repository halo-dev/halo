import BasicLayout from "@console/layouts/BasicLayout.vue";
import { definePlugin } from "@halo-dev/ui-shared";

// Temporary VTable preview. Delete this directory after review.
export default definePlugin({
  routes: [
    {
      path: "/table-demo",
      name: "TableDemoRoot",
      component: BasicLayout,
      meta: {
        searchable: false,
        hideFooter: true,
      },
      children: [
        {
          path: "",
          name: "TableDemo",
          component: () => import("./TableDemo.vue"),
        },
      ],
    },
    {
      path: "/plugins-table-demo",
      name: "PluginsTableDemoRoot",
      component: BasicLayout,
      meta: {
        searchable: false,
        hideFooter: true,
      },
      children: [
        {
          path: "",
          name: "PluginsTableDemo",
          component: () =>
            import("@console/modules/system/plugins/PluginListTable.vue"),
        },
      ],
    },
  ],
});
