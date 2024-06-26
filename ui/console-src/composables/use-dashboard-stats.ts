import { consoleApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";

export function useDashboardStats() {
  const { data } = useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: async () => {
      const { data } = await consoleApiClient.system.getStats();
      return data;
    },
  });
  return { data };
}
