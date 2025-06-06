import { usePluginModuleStore } from "@/stores/plugin";
import type { DashboardWidgetQuickActionItem } from "@halo-dev/console-shared";
import { onMounted, ref } from "vue";

const EXTENSION_POINT_NAME =
  "console:dashboard:widgets:internal:quick-action:item:create";

export function useDashboardQuickActionExtensionPoint() {
  const { pluginModuleMap } = usePluginModuleStore();

  const quickActionItems = ref<DashboardWidgetQuickActionItem[]>([]);

  onMounted(async () => {
    const result: DashboardWidgetQuickActionItem[] = [];
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

        result.push(...definitions);
      } catch (error) {
        console.error(`Error processing plugin module:`, name, error);
      }
    }
    quickActionItems.value = result;
  });

  return { quickActionItems };
}
