import { definePlugin } from "@halo-dev/ui-shared";
import { defineAsyncComponent } from "vue";

export default definePlugin({
  components: {
    AttachmentSelectorModal: defineAsyncComponent(
      () => import("./components/AttachmentSelectorModal.vue")
    ),
  },
});
