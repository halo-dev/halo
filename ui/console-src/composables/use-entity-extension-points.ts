import type { EntityFieldItem } from "@halo-dev/ui-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed, markRaw, toValue, type ComputedRef, type Ref } from "vue";
import { usePluginModuleStore } from "@/stores/plugin";

export function useEntityFieldItemExtensionPoint<T>(
  extensionPointName: string,
  entity: Ref<T>,
  presets: ComputedRef<EntityFieldItem[]>
) {
  const { pluginModules } = usePluginModuleStore();

  return useQuery({
    // Deep structural sharing would clone component definitions and lose markRaw.
    structuralSharing: false,
    queryKey: computed(() => [
      "core:extension-points:list-item:fields",
      extensionPointName,
      toValue(entity),
    ]),
    queryFn: async () => {
      const itemsFromPlugins: EntityFieldItem[] = [];

      for (const pluginModule of pluginModules) {
        const { extensionPoints } = pluginModule;
        if (!extensionPoints?.[extensionPointName]) {
          continue;
        }
        const items = extensionPoints[extensionPointName](
          entity
        ) as EntityFieldItem[];
        for (const item of items) {
          markRaw(item.component);
        }
        itemsFromPlugins.push(...items);
      }

      const allItems = [...presets.value, ...itemsFromPlugins].sort(
        (a, b) => a.priority - b.priority
      );

      const start: EntityFieldItem[] = [];
      const end: EntityFieldItem[] = [];

      for (const item of allItems) {
        if (item.position === "start") {
          start.push(item);
        } else if (item.position === "end") {
          end.push(item);
        }
      }

      return {
        start,
        end,
      };
    },
    enabled: computed(() => !!presets.value && !!entity.value),
  });
}
