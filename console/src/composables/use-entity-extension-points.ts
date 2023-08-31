import { usePluginModuleStore } from "@/stores/plugin";
import { usePermission } from "@/utils/permission";
import type {
  EntityDropdownItem,
  EntityFieldItem,
  PluginModule,
} from "@halo-dev/console-shared";
import { onMounted, ref, type Ref, computed, type ComputedRef } from "vue";

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
  entity: Ref<T>,
  presets: ComputedRef<EntityFieldItem[]>
) {
  const { pluginModules } = usePluginModuleStore();
  const { currentUserHasPermission } = usePermission();
  const itemsFromPlugins = ref<EntityFieldItem[]>([]);

  const allItems = computed(() => {
    return [...presets.value, ...itemsFromPlugins.value].map((item) => {
      return {
        ...item,
        visible:
          item.visible !== false && currentUserHasPermission(item.permissions),
      };
    });
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
