import { ucApiClient } from "@halo-dev/api-client";
import type {
  DashboardResponsiveLayout,
  DashboardWidget,
} from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { computed, ref, type Ref } from "vue";
import { DefaultResponsiveLayouts } from "../widgets/defaults";

export function useDashboardWidgetsFetch(breakpoint: Ref<string>) {
  const layouts = ref<DashboardResponsiveLayout>({});
  const layout = ref<DashboardWidget[]>([]);
  const originalLayout = ref<DashboardWidget[]>([]);

  const { isLoading } = useQuery({
    queryKey: ["core:dashboard:widgets", breakpoint],
    queryFn: async () => {
      const { data } = await ucApiClient.user.preference.getMyPreference({
        group: "dashboard-widgets",
      });
      if (!data) {
        return null;
      }
      return data as DashboardResponsiveLayout;
    },
    cacheTime: 0,
    onSuccess: (data) => {
      layouts.value = data || DefaultResponsiveLayouts;

      const layoutData =
        layouts.value[breakpoint.value] || layouts.value["lg"] || [];

      layout.value = layoutData;
      originalLayout.value = cloneDeep(layoutData);
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

export function useDashboardWidgetsViewFetch(breakpoint: Ref<string>) {
  return useQuery({
    queryKey: ["core:dashboard:widgets:view", breakpoint],
    queryFn: async () => {
      const { data } = await ucApiClient.user.preference.getMyPreference({
        group: "dashboard-widgets",
      });

      const layouts = (data ||
        DefaultResponsiveLayouts) as DashboardResponsiveLayout;

      return {
        layouts,
        layout: layouts[breakpoint.value] || layouts.lg || [],
      };
    },
    enabled: computed(() => !!breakpoint.value),
  });
}
