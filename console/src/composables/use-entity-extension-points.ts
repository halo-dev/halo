import { usePluginModuleStore } from "@/stores/plugin";
import type {
  EntityDropdownItem,
  PluginModule,
} from "@halo-dev/console-shared";
import { onMounted, ref, type ComputedRef, type Ref, computed } from "vue";

export function useEntityDropdownItemExtensionPoint<T>(
  extensionPointName: string,
  entity: Ref<T>,
  presets: ComputedRef<EntityDropdownItem<T>[]>
) {
  const { pluginModules } = usePluginModuleStore();

  const itemsFromPlugins = ref<EntityDropdownItem<T>[]>([]);

  onMounted(() => {
    pluginModules.forEach((pluginModule: PluginModule) => {
      const { extensionPoints } = pluginModule;
      if (!extensionPoints?.[extensionPointName]) {
        return;
      }

      const items = extensionPoints[extensionPointName](
        entity
      ) as EntityDropdownItem<T>[];

      itemsFromPlugins.value.push(...items);
    });
  });

  const dropdownItems = computed(() => {
    return [...presets.value, ...itemsFromPlugins.value].sort((a, b) => {
      return a.priority - b.priority;
    });
  });

  return { dropdownItems };
}
