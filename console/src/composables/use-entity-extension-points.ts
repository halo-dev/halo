import { usePluginModuleStore } from "@/stores/plugin";
import { usePermission } from "@/utils/permission";
import type {
  EntityDropdownItem,
  EntityFieldItem,
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

export function useEntityFieldItemExtensionPoint<T>(
  extensionPointName: string,
  item: T,
  presets: EntityFieldItem[]
) {
  const { pluginModules } = usePluginModuleStore();
  const { currentUserHasPermission } = usePermission();

  const startFields = ref<EntityFieldItem[]>([]);
  const endFields = ref<EntityFieldItem[]>([]);

  onMounted(() => {
    const itemsFromPlugins: EntityFieldItem[] = [];
    pluginModules.forEach((pluginModule: PluginModule) => {
      const { extensionPoints } = pluginModule;
      if (!extensionPoints?.[extensionPointName]) {
        return;
      }
      const items = extensionPoints[extensionPointName](
        item
      ) as EntityFieldItem[];
      itemsFromPlugins.push(...items);
    });

    const allItems = [...presets, ...itemsFromPlugins]
      .sort((a, b) => {
        return a.priority - b.priority;
      })
      .map((item) => {
        return {
          ...item,
          visible:
            item.visible !== false &&
            currentUserHasPermission(item.permissions),
        };
      });

    startFields.value = allItems.filter((item) => item.position === "start");
    endFields.value = allItems.filter((item) => item.position === "end");
  });

  return { startFields, endFields };
}
