import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@uc/layouts/BasicLayout.vue";
import { IconBookRead } from "@halo-dev/components";
import PostList from "./PostList.vue";
import { markRaw } from "vue";
import PostEditor from "./PostEditor.vue";

export default definePlugin({
  ucRoutes: [
    {
      path: "/posts",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Posts",
          component: PostList,
          meta: {
            title: "core.uc_post.title",
            searchable: true,
            permissions: ["uc:posts:manage"],
            menu: {
              name: "core.uc_sidebar.menu.items.posts",
              group: "content",
              icon: markRaw(IconBookRead),
              priority: 0,
              mobile: true,
            },
          },
        },
        {
          path: "editor",
          name: "PostEditor",
          component: PostEditor,
          meta: {
            title: "core.post_editor.title",
            searchable: true,
            permissions: ["uc:posts:manage"],
          },
        },
      ],
    },
  ],
});
