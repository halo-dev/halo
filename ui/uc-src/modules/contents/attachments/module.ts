import { definePlugin } from "@halo-dev/ui-shared";
import { defineAsyncComponent } from "vue";

declare module "vue" {
  interface GlobalComponents {
    AttachmentSelectorModal: (typeof import("./components/AttachmentSelectorModal.vue"))["default"];
  }
}

export default definePlugin({
  components: {
    AttachmentSelectorModal: defineAsyncComponent(
      () => import("./components/AttachmentSelectorModal.vue")
    ),
  },
});
