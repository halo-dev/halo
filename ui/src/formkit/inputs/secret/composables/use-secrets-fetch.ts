import {
  coreApiClient,
  paginate,
  type Secret,
  type SecretV1alpha1ApiListSecretRequest,
} from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";

export const Q_KEY = () => ["secrets"];

export function useSecretsFetch() {
  return useQuery({
    queryKey: Q_KEY(),
    queryFn: async () => {
      return await paginate<SecretV1alpha1ApiListSecretRequest, Secret>(
        (params) => coreApiClient.secret.listSecret(params),
        {
          size: 1000,
        }
      );
    },
    refetchInterval(data) {
      const hasDeletingData = data?.some(
        (item) => !!item.metadata.deletionTimestamp
      );
      return hasDeletingData ? 1000 : false;
    },
  });
}
