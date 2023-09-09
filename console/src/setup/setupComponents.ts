import { defineAsyncComponent, type App } from "vue";
import { VClosePopper, VTooltip } from "@halo-dev/components";
import { Dropdown } from "floating-vue";
import "floating-vue/dist/style.css";
// @ts-ignore
import VueGridLayout from "vue-grid-layout";
import { defaultConfig, plugin as FormKit } from "@formkit/vue";
import FormKitConfig from "@/formkit/formkit.config";
import FilterDropdown from "@/components/filter/FilterDropdown.vue";
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import SearchInput from "@/components/input/SearchInput.vue";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import AttachmentFileTypeIcon from "@/components/icon/AttachmentFileTypeIcon.vue";

export function setupComponents(app: App) {
  app.use(VueGridLayout);
  app.use(
    FormKit,
    defaultConfig({
      ...FormKitConfig,
    })
  );

  app.directive("tooltip", VTooltip);
  app.directive("close-popper", VClosePopper);
  // @deprecated
  // Will be removed in the future, please use the VDropdown component from @halo-dev/components.
  app.component("FloatingDropdown", Dropdown);
  app.component(
    "VCodemirror",
    defineAsyncComponent(() => import("@/components/codemirror/Codemirror.vue"))
  );

  // Console components
  app.component("FilterDropdown", FilterDropdown);
  app.component("FilterCleanButton", FilterCleanButton);
  app.component("SearchInput", SearchInput);
  app.component("AnnotationsForm", AnnotationsForm);
  app.component("AttachmentFileTypeIcon", AttachmentFileTypeIcon);
}
