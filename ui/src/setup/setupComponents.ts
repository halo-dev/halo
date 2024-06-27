import { vClosePopper, VLoading, vTooltip } from "@halo-dev/components";
import { Dropdown } from "floating-vue";
import "floating-vue/dist/style.css";
import { defineAsyncComponent, type App } from "vue";
// @ts-ignore
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import FilterDropdown from "@/components/filter/FilterDropdown.vue";
import AnnotationsForm from "@/components/form/AnnotationsForm.vue";
import AttachmentFileTypeIcon from "@/components/icon/AttachmentFileTypeIcon.vue";
import SearchInput from "@/components/input/SearchInput.vue";
import HasPermission from "@/components/permission/HasPermission.vue";
import FormKitConfig from "@/formkit/formkit.config";
import { defaultConfig, plugin as FormKit } from "@formkit/vue";
import VueGridLayout from "vue-grid-layout";

export function setupComponents(app: App) {
  app.use(VueGridLayout);
  app.use(
    FormKit,
    defaultConfig({
      ...FormKitConfig,
    })
  );

  app.directive("tooltip", vTooltip);
  app.directive("close-popper", vClosePopper);
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
  app.component("HasPermission", HasPermission);
  app.component(
    "UppyUpload",
    defineAsyncComponent({
      loader: () => import("@/components/upload/UppyUpload.vue"),
      loadingComponent: VLoading,
    })
  );
}
