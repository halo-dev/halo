import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import BlankLayout from "@/layouts/BlankLayout.vue";
import { IconBookRead } from "@halo-dev/components";
import PostList from "./PostList.vue";
import DeletedPostList from "./DeletedPostList.vue";
import PostEditor from "./PostEditor.vue";
import CategoryList from "./categories/CategoryList.vue";
import TagList from "./tags/TagList.vue";
import PostStatsWidget from "./widgets/PostStatsWidget.vue";
import RecentPublishedWidget from "./widgets/RecentPublishedWidget.vue";
import { markRaw } from "vue";

export default definePlugin({
  components: {
    PostStatsWidget,
    RecentPublishedWidget,
  },
  routes: [
    {
      path: "/posts",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Posts",
          component: PostList,
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
        },
        {
          path: "deleted",
          name: "DeletedPosts",
          component: DeletedPostList,
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
              component: CategoryList,
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
              component: TagList,
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
