import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";

export const Q_KEY = () => ["secrets"];

export function useSecretsFetch() {
  return useQuery({
    queryKey: Q_KEY(),
    queryFn: async () => {
      const { data } = await coreApiClient.secret.listSecret();
      return data;
    },
    refetchInterval(data) {
      const hasDeletingData = data?.items.some(
        (item) => !!item.metadata.deletionTimestamp
      );
      return hasDeletingData ? 1000 : false;
    },
  });
}
