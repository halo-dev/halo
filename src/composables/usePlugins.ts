import { usePluginStore } from "@/stores/plugin";
import type {
  ExtensionPointName,
  ExtensionPointState,
} from "@halo-dev/admin-shared";
import type { Plugin } from "@/types/extension";
import type { Ref } from "vue";

export function useExtensionPointsState(
  point: ExtensionPointName,
  state: Ref<ExtensionPointState>
) {
  const { plugins } = usePluginStore();

  plugins.forEach((plugin: Plugin) => {
    if (!plugin.spec.module?.extensionPoints?.[point]) {
      return;
    }
    plugin.spec.module.extensionPoints[point]?.(state);
  });
}
