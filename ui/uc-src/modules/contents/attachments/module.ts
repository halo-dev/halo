import { definePlugin } from "@halo-dev/console-shared";
import { defineAsyncComponent } from "vue";

export default definePlugin({
  components: {
    AttachmentSelectorModal: defineAsyncComponent(
      () => import("./components/AttachmentSelectorModal.vue")
    ),
  },
});
