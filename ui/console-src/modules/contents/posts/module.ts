import BasicLayout from "@console/layouts/BasicLayout.vue";
import BlankLayout from "@console/layouts/BlankLayout.vue";
import { IconBookRead } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import PostEditor from "./PostEditor.vue";
import PostList from "./PostList.vue";

export default definePlugin({
  routes: [
    {
      path: "/posts",
      name: "PostsRoot",
      component: BasicLayout,
      meta: {
        title: "core.post.title",
        searchable: true,
        permissions: ["system:posts:view"],
        menu: {
          name: "core.sidebar.menu.items.posts",
          group: "content",
          icon: markRaw(IconBookRead),
          priority: 0,
          mobile: true,
        },
      },
      children: [
        {
          path: "",
          name: "Posts",
          component: PostList,
        },
        {
          path: "deleted",
          name: "DeletedPosts",
          component: () => import("./DeletedPostList.vue"),
          meta: {
            title: "core.deleted_post.title",
            searchable: true,
            permissions: ["system:posts:view"],
          },
        },
        {
          path: "editor",
          name: "PostEditor",
          component: PostEditor,
          meta: {
            title: "core.post_editor.title",
            searchable: true,
            hideFooter: true,
            permissions: ["system:posts:manage"],
          },
        },
        {
          path: "snapshots",
          name: "PostSnapshots",
          component: () => import("./PostSnapshots.vue"),
          meta: {
            title: "core.post_snapshots.title",
            searchable: false,
            hideFooter: true,
            permissions: ["system:posts:manage"],
          },
        },
        {
          path: "categories",
          component: BlankLayout,
          children: [
            {
              path: "",
              name: "Categories",
              component: () => import("./categories/CategoryList.vue"),
              meta: {
                title: "core.post_category.title",
                searchable: true,
                permissions: ["system:posts:view"],
              },
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
              component: () => import("./tags/TagList.vue"),
              meta: {
                title: "core.post_tag.title",
                searchable: true,
                permissions: ["system:posts:view"],
              },
            },
          ],
        },
      ],
    },
  ],
});
