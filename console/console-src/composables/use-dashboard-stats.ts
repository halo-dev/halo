import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";

export function useDashboardStats() {
  const { data } = useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: async () => {
      const { data } = await apiClient.stats.getStats();
      return data;
    },
  });
  return { data };
}
