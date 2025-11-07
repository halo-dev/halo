import { usePluginModuleStore } from "@/stores/plugin";
import type { OperationItem } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed, toValue, type ComputedRef, type Ref } from "vue";

export function useOperationItemExtensionPoint<T>(
  extensionPointName: string,
  entity: Ref<T>,
  presets: ComputedRef<OperationItem<T>[]>
) {
  const { pluginModules } = usePluginModuleStore();

  return useQuery({
    queryKey: computed(() => [
      "core:extension-points:operation-items",
      extensionPointName,
      toValue(entity),
    ]),
    queryFn: async () => {
      const itemsFromPlugins: OperationItem<T>[] = [];
      for (const pluginModule of pluginModules) {
        const { extensionPoints } = pluginModule;
        if (!extensionPoints?.[extensionPointName]) {
          continue;
        }

        const items = extensionPoints[extensionPointName](
          entity
        ) as OperationItem<T>[];

        itemsFromPlugins.push(...items);
      }

      return [...presets.value, ...itemsFromPlugins].sort(
        (a, b) => a.priority - b.priority
      );
    },
    enabled: computed(() => !!presets.value && !!entity.value),
  });
}
