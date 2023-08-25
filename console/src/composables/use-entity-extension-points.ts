import { usePluginModuleStore } from "@/stores/plugin";
import type {
  EntityDropdownItem,
  PluginModule,
} from "@halo-dev/console-shared";
import { onMounted, ref } from "vue";

export function useEntityDropdownItemExtensionPoint<T>(
  extensionPointName: string,
  presets: EntityDropdownItem<T>[]
) {
  const { pluginModules } = usePluginModuleStore();

  const dropdownItems = ref<EntityDropdownItem<T>[]>(presets);

  onMounted(() => {
    pluginModules.forEach((pluginModule: PluginModule) => {
      const { extensionPoints } = pluginModule;
      if (!extensionPoints?.[extensionPointName]) {
        return;
      }

      const items = extensionPoints[
        extensionPointName
      ]() as EntityDropdownItem<T>[];

      dropdownItems.value.push(...items);
    });

    dropdownItems.value.sort((a, b) => {
      return a.priority - b.priority;
    });
  });

  return { dropdownItems };
}
