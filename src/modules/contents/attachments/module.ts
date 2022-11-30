import { definePlugin } from "@halo-dev/console-shared";
import BasicLayout from "@/layouts/BasicLayout.vue";
import AttachmentList from "./AttachmentList.vue";
import AttachmentSelectorModal from "./components/AttachmentSelectorModal.vue";
import { IconFolder } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {
    AttachmentSelectorModal,
  },
  routes: [
    {
      path: "/attachments",
      component: BasicLayout,
      children: [
        {
          path: "",
          name: "Attachments",
          component: AttachmentList,
          meta: {
            title: "附件",
            permissions: ["system:attachments:view"],
            menu: {
              name: "附件",
              group: "content",
              icon: markRaw(IconFolder),
              priority: 3,
              mobile: true,
            },
          },
        },
      ],
    },
  ],
});
