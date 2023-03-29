import type { App } from "vue";
import { VClosePopper, VTooltip } from "@halo-dev/components";
import { Dropdown } from "floating-vue";
import "floating-vue/dist/style.css";
// @ts-ignore
import VueGridLayout from "vue-grid-layout";
import { defaultConfig, plugin as FormKit } from "@formkit/vue";
import FormKitConfig from "@/formkit/formkit.config";

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
}
