import { usePluginStore } from "@/stores/plugin";
import type {
  ExtensionPointName,
  ExtensionPointState,
} from "@halo-dev/admin-shared";
import type { Plugin } from "@halo-dev/api-client";
import type { Ref } from "vue";

export function useExtensionPointsState(
  point: ExtensionPointName,
  state: Ref<ExtensionPointState>
) {
  const { plugins } = usePluginStore();

  plugins.forEach((plugin: Plugin) => {
    // @ts-ignore
    if (!plugin.spec.module?.extensionPoints?.[point]) {
      return;
    }
    // @ts-ignore
    plugin.spec.module.extensionPoints[point]?.(state);
  });
}
