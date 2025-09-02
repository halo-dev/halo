import { IconBookRead } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@uc/layouts/BasicLayout.vue";
import { markRaw } from "vue";
import IssueList from "./IssueList.vue";

export default definePlugin({
  ucRoutes: [
    {
      path: "/issues",
      name: "IssuesRoot",
      component: BasicLayout,
      meta: {
        title: "core.uc_issue.title",
        searchable: true,
        permissions: ["uc:issues:manage"],
        menu: {
          name: "core.uc_sidebar.menu.items.issues",
          group: "content",
          icon: markRaw(IconBookRead),
          priority: 1,
          mobile: true,
        },
      },
      children: [
        {
          path: "",
          name: "Issues",
          component: IssueList,
        },
      ],
    },
  ],
});