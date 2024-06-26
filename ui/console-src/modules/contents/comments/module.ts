import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconMessage } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import CommentList from "./CommentList.vue";
import CommentStatsWidget from "./widgets/CommentStatsWidget.vue";

export default definePlugin({
  components: {
    CommentStatsWidget,
  },
  routes: [
    {
      path: "/comments",
      name: "CommentsRoot",
      component: BasicLayout,
      meta: {
        title: "core.comment.title",
        searchable: true,
        permissions: ["system:comments:view"],
        menu: {
          name: "core.sidebar.menu.items.comments",
          group: "content",
          icon: markRaw(IconMessage),
          priority: 2,
          mobile: true,
        },
      },
      children: [
        {
          path: "",
          name: "Comments",
          component: CommentList,
        },
      ],
    },
  ],
});
