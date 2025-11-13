import BasicLayout from "@console/layouts/BasicLayout.vue";
import { IconFolder } from "@halo-dev/components";
import { definePlugin } from "@halo-dev/ui-shared";
import { defineAsyncComponent, markRaw } from "vue";

export default definePlugin({
  components: {
    AttachmentSelectorModal: defineAsyncComponent(
      () => import("./components/AttachmentSelectorModal.vue")
    ),
  },
  routes: [
    {
      path: "/attachments",
      name: "AttachmentsRoot",
      component: BasicLayout,
      meta: {
        title: "core.attachment.title",
        permissions: ["system:attachments:view"],
        menu: {
          name: "core.sidebar.menu.items.attachments",
          group: "content",
          icon: markRaw(IconFolder),
          priority: 3,
          mobile: true,
        },
      },
      children: [
        {
          path: "",
          name: "Attachments",
          component: () => import("./AttachmentList.vue"),
        },
      ],
    },
  ],
});
