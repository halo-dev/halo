import { usePluginModuleStore } from "@/stores/plugin";
import type { OperationItem, PluginModule } from "@halo-dev/console-shared";
import { onMounted, ref, type ComputedRef, type Ref, computed } from "vue";

export function useOperationItemExtensionPoint<T>(
  extensionPointName: string,
  entity: Ref<T>,
  presets: ComputedRef<OperationItem<T>[]>
) {
  const { pluginModules } = usePluginModuleStore();

  const itemsFromPlugins = ref<OperationItem<T>[]>([]);

  onMounted(() => {
    pluginModules.forEach((pluginModule: PluginModule) => {
      const { extensionPoints } = pluginModule;
      if (!extensionPoints?.[extensionPointName]) {
        return;
      }

      const items = extensionPoints[extensionPointName](
        entity
      ) as OperationItem<T>[];

      itemsFromPlugins.value.push(...items);
    });
  });

  const operationItems = computed(() => {
    return [...presets.value, ...itemsFromPlugins.value].sort((a, b) => {
      return a.priority - b.priority;
    });
  });

  return { operationItems };
}
