import { consoleApiClient } from "@halo-dev/api-client";
import type {
  DashboardResponsiveLayout,
  DashboardWidget,
} from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { ref, type Ref } from "vue";

export function useDashboardWidgetsFetch(breakpoint: Ref<string>) {
  const layouts = ref<DashboardResponsiveLayout>({});
  const layout = ref<DashboardWidget[]>([]);

  const { isLoading } = useQuery({
    queryKey: ["core:dashboard:widgets", breakpoint.value],
    queryFn: async () => {
      const { data } =
        await consoleApiClient.plugin.plugin.fetchPluginJsonConfig({
          name: "app-store-integration",
        });
      const configMapData = data as Record<string, unknown>;
      return configMapData.dashboard as DashboardResponsiveLayout;
    },
    onSuccess: (data) => {
      layouts.value = data;
      layout.value = data[breakpoint.value] || data["lg"] || [];
    },
  });

  return {
    layouts,
    layout,
    isLoading,
  };
}
