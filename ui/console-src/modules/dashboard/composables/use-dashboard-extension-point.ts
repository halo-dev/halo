import { usePluginModuleStore } from "@/stores/plugin";
import type { DashboardWidgetDefinition } from "@halo-dev/console-shared";
import { onMounted, ref } from "vue";

const EXTENSION_POINT_NAME = "console:dashboard:widgets:create";

export function useDashboardExtensionPoint() {
  const { pluginModuleMap } = usePluginModuleStore();

  const widgetDefinitions = ref<DashboardWidgetDefinition[]>([]);

  onMounted(async () => {
    const finalDefinitions: DashboardWidgetDefinition[] = [];
    for (const [name, module] of Object.entries(pluginModuleMap)) {
      try {
        const callbackFunction =
          module?.extensionPoints?.[EXTENSION_POINT_NAME];

        if (typeof callbackFunction !== "function") {
          continue;
        }

        const definitions = await callbackFunction();

        // Reset id
        definitions.forEach((definition) => {
          definition.id = `${name}-${definition.id}`;
        });

        finalDefinitions.push(...definitions);
      } catch (error) {
        console.error(`Error processing plugin module:`, name, error);
      }
    }
    widgetDefinitions.value = finalDefinitions;
  });

  return { widgetDefinitions };
}
