import { ucApiClient } from "@halo-dev/api-client";
import type {
  DashboardResponsiveLayout,
  DashboardWidget,
} from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref, type Ref } from "vue";

export function useDashboardWidgetsFetch(breakpoint: Ref<string>) {
  const layouts = ref<DashboardResponsiveLayout>({});
  const layout = ref<DashboardWidget[]>([]);

  const { isLoading } = useQuery({
    queryKey: ["core:dashboard:widgets", breakpoint],
    queryFn: async () => {
      const { data } = await ucApiClient.user.preference.getMyPreference({
        group: "dashboard-widgets",
      });
      if (!data) {
        return {};
      }
      return data as DashboardResponsiveLayout;
    },
    onSuccess: (data) => {
      layouts.value = data;
      layout.value = data[breakpoint.value] || data["lg"] || [];
    },
    enabled: computed(() => !!breakpoint.value),
  });

  return {
    layouts,
    layout,
    isLoading,
  };
}
