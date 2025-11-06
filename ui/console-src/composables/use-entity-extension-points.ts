import { usePluginModuleStore } from "@/stores/plugin";
import type { EntityFieldItem } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed, type ComputedRef, type Ref, toValue } from "vue";

export function useEntityFieldItemExtensionPoint<T>(
  extensionPointName: string,
  entity: Ref<T>,
  presets: ComputedRef<EntityFieldItem[]>
) {
  const { pluginModules } = usePluginModuleStore();

  return useQuery({
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
