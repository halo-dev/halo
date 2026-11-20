import { definePlugin } from "@halo-dev/ui-shared";
import BasicLayout from "@uc/layouts/BasicLayout.vue";

// Temporary PostList table preview. Delete this directory after review.
export default definePlugin({
  ucRoutes: [
    {
      path: "/posts-table-demo",
      name: "PostsTableDemoRoot",
      component: BasicLayout,
      meta: {
        title: "我的文章（表格预览）",
        searchable: false,
        hideFooter: true,
      },
      children: [
        {
          path: "",
          name: "PostsTableDemo",
          component: () =>
            import("@uc/modules/contents/posts/PostListTable.vue"),
        },
      ],
    },
  ],
});
