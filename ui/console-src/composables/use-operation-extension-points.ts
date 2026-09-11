import type { OperationItem } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed, markRaw, toValue, type ComputedRef, type Ref } from "vue";
import { usePluginModuleStore } from "@/stores/plugin";

function markOperationComponentsRaw<T>(items: OperationItem<T>[]) {
  for (const item of items) {
    if (item.component) {
      markRaw(item.component);
    }
    if (item.children) {
      markOperationComponentsRaw(item.children);
    }
  }
}

export function useOperationItemExtensionPoint<T>(
  extensionPointName: string,
  entity: Ref<T>,
  presets: ComputedRef<OperationItem<T>[]>
) {
  const { pluginModules } = usePluginModuleStore();

  const query = useQuery({
    // Deep structural sharing would clone component definitions and lose markRaw.
    structuralSharing: false,
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

        markOperationComponentsRaw(items);
        itemsFromPlugins.push(...items);
      }

      return itemsFromPlugins;
    },
    enabled: computed(() => !!presets.value && !!entity.value),
  });

  return {
    ...query,
    data: computed(() =>
      query.data.value
        ? [...presets.value, ...query.data.value].sort(
            (a, b) => a.priority - b.priority
          )
        : undefined
    ),
  };
}
