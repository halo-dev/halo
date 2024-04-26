import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@console/layouts/BasicLayout.vue";
import BlankLayout from "@console/layouts/BlankLayout.vue";
import { IconBookRead } from "@halo-dev/components";
import PostList from "./PostList.vue";
import DeletedPostList from "./DeletedPostList.vue";
import PostEditor from "./PostEditor.vue";
import CategoryList from "./categories/CategoryList.vue";
import TagList from "./tags/TagList.vue";
import PostStatsWidget from "./widgets/PostStatsWidget.vue";
import RecentPublishedWidget from "./widgets/RecentPublishedWidget.vue";
import { markRaw } from "vue";
import PostSnapshots from "./PostSnapshots.vue";

export default definePlugin({
  components: {
    PostStatsWidget,
    RecentPublishedWidget,
  },
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
            hideFooter: true,
            permissions: ["system:posts:manage"],
          },
        },
        {
          path: "snapshots",
          name: "PostSnapshots",
          component: PostSnapshots,
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
