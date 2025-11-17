import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconMessage, VLoading } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { defineAsyncComponent, markRaw } from "vue";
import SubjectQueryCommentListModal from "./components/SubjectQueryCommentListModal.vue";

declare module "vue" {
  interface GlobalComponents {
    SubjectQueryCommentList: (typeof import("./components/SubjectQueryCommentList.vue"))["default"];
    SubjectQueryCommentListModal: (typeof import("./components/SubjectQueryCommentListModal.vue"))["default"];
  }
}

export default definePlugin({
  components: {
    SubjectQueryCommentList: defineAsyncComponent({
      loader: () => import("./components/SubjectQueryCommentList.vue"),
      loadingComponent: VLoading,
    }),
    SubjectQueryCommentListModal,
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
          component: () => import("./CommentList.vue"),
        },
      ],
    },
  ],
});
