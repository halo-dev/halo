import { definePlugin, utils } from "@halo-dev/ui-shared";
import { defineAsyncComponent } from "vue";

export default definePlugin({
  components: {
    AttachmentSelectorModal: defineAsyncComponent({
      loader: () => {
        if (utils.permission.has(["system:attachments:manage"])) {
          return import(
            "@console/modules/contents/attachments/components/AttachmentSelectorModal.vue"
          );
        }
        return import(
          "@uc/modules/contents/attachments/components/AttachmentSelectorModal.vue"
        );
      },
    }),
  },
});
