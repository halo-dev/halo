import type { Plugin } from "@halo-dev/admin-shared";
import { BasicLayout } from "@/layouts";
import { IconMessage } from "@halo-dev/components";
import CommentList from "./CommentList.vue";

const commentModule: Plugin = {
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
};

export default commentModule;
