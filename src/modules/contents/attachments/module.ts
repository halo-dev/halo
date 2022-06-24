import { BasicLayout, definePlugin } from "@halo-dev/admin-shared";
import AttachmentList from "./AttachmentList.vue";
import { IconFolder } from "@halo-dev/components";

export default definePlugin({
  name: "attachmentModule",
  components: [],
  routes: [
    {
      path: "/attachments",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Attachments",
          component: AttachmentList,
        },
      ],
    },
  ],
  menus: [
    {
      name: "内容",
      items: [
        {
          name: "附件",
          path: "/attachments",
          icon: IconFolder,
        },
      ],
    },
  ],
});
