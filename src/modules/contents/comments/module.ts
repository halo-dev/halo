import { BasicLayout, definePlugin } from "@halo-dev/admin-shared";
import { IconMessage } from "@halo-dev/components";
import CommentList from "./CommentList.vue";

export default definePlugin({
  name: "commentModule",
  components: [],
  routes: [
    {
      path: "/comments",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Comments",
          component: CommentList,
        },
      ],
    },
  ],
  menus: [
    {
      name: "内容",
      items: [
        {
          name: "评论",
          path: "/comments",
          icon: IconMessage,
        },
      ],
    },
  ],
});
