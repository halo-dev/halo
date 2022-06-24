import { BasicLayout, BlankLayout, definePlugin } from "@halo-dev/admin-shared";
import { IconBookRead } from "@halo-dev/components";
import PostList from "./PostList.vue";
import PostEditor from "./PostEditor.vue";
import CategoryList from "./categories/CategoryList.vue";
import TagList from "./tags/TagList.vue";

export default definePlugin({
  name: "postModule",
  components: [],
  routes: [
    {
      path: "/posts",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Posts",
          component: PostList,
        },
        {
          path: "editor",
          name: "PostEditor",
          component: PostEditor,
        },
        {
          path: "categories",
          component: BlankLayout,
          children: [
            {
              path: "",
              name: "Categories",
              component: CategoryList,
            },
          ],
        },
        {
          path: "tags",
          component: BlankLayout,
          children: [
            {
              path: "",
              name: "Tags",
              component: TagList,
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
          name: "文章",
          path: "/posts",
          icon: IconBookRead,
        },
      ],
    },
  ],
});
