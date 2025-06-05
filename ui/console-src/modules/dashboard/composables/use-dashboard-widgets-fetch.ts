import { ucApiClient } from "@halo-dev/api-client";
import type {
  DashboardResponsiveLayout,
  DashboardWidget,
} from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { computed, ref, type Ref } from "vue";

export function useDashboardWidgetsFetch(breakpoint: Ref<string>) {
  const layouts = ref<DashboardResponsiveLayout>({});
  const layout = ref<DashboardWidget[]>([]);
  const originalLayout = ref<DashboardWidget[] | undefined>();

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
    cacheTime: 0,
    onSuccess: (data) => {
      layouts.value = data;
      layout.value = data[breakpoint.value] || data["lg"] || [];

      if (!originalLayout.value) {
        originalLayout.value = cloneDeep(
          layout.value.length > 0 ? layout.value : undefined
        );
      }
    },
    enabled: computed(() => !!breakpoint.value),
  });

  return {
    layouts,
    layout,
    originalLayout,
    isLoading,
  };
}
