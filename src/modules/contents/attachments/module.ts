import type { Plugin } from "@halo-dev/admin-shared";
import { BasicLayout } from "@/layouts";
import AttachmentList from "./AttachmentList.vue";
import { IconFolder } from "@halo-dev/components";

const attachmentModule: Plugin = {
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
};

export default attachmentModule;
