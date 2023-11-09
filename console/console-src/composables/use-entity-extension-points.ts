import { usePluginModuleStore } from "@/stores/plugin";
import type { EntityFieldItem, PluginModule } from "@halo-dev/console-shared";
import { onMounted, ref, type ComputedRef, type Ref, computed } from "vue";

export function useEntityFieldItemExtensionPoint<T>(
  extensionPointName: string,
  entity: Ref<T>,
  presets: ComputedRef<EntityFieldItem[]>
) {
  const { pluginModules } = usePluginModuleStore();
  const itemsFromPlugins = ref<EntityFieldItem[]>([]);

  const allItems = computed(() => {
    return [...presets.value, ...itemsFromPlugins.value];
  });

  onMounted(() => {
    pluginModules.forEach((pluginModule: PluginModule) => {
      const { extensionPoints } = pluginModule;
      if (!extensionPoints?.[extensionPointName]) {
        return;
      }
      const items = extensionPoints[extensionPointName](
        entity
      ) as EntityFieldItem[];
      itemsFromPlugins.value.push(...items);
    });
  });

  const startFields = computed(() => {
    return allItems.value
      .filter((item) => item.position === "start")
      .sort((a, b) => {
        return a.priority - b.priority;
      });
  });

  const endFields = computed(() => {
    return allItems.value
      .filter((item) => item.position === "end")
      .sort((a, b) => {
        return a.priority - b.priority;
      });
  });

  return { startFields, endFields };
}
