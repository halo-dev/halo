import { usePluginModuleStore } from "@/stores/plugin";
import type { DashboardWidgetDefinition } from "packages/shared/dist";
import { onMounted, ref } from "vue";

const EXTENSION_POINT_NAME = "console:dashboard:widgets:create";

export function useDashboardExtensionPoint() {
  const { pluginModules } = usePluginModuleStore();

  const widgetDefinitions = ref<DashboardWidgetDefinition[]>([]);

  onMounted(async () => {
    const items: DashboardWidgetDefinition[] = [];
    for (const pluginModule of pluginModules) {
      try {
        const callbackFunction =
          pluginModule?.extensionPoints?.[EXTENSION_POINT_NAME];

        if (typeof callbackFunction !== "function") {
          continue;
        }

        items.push(...(await callbackFunction()));
      } catch (error) {
        console.error(`Error processing plugin module:`, pluginModule, error);
      }
    }
    widgetDefinitions.value = items;
  });

  return { widgetDefinitions };
}
