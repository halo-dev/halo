import { paginate } from "@/utils/paginate";
import type {
  ExtensionDefinition,
  ExtensionDefinitionV1alpha1ApiListExtensionDefinitionRequest,
  ExtensionPointDefinition,
} from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { computed, type Ref } from "vue";

export function useExtensionDefinitionFetch(
  extensionPointDefinition: Ref<ExtensionPointDefinition | undefined>
) {
  return useQuery({
    queryKey: ["extension-definitions", extensionPointDefinition],
    queryFn: async () => {
      return await paginate<
        ExtensionDefinitionV1alpha1ApiListExtensionDefinitionRequest,
        ExtensionDefinition
      >(
        (params) =>
          coreApiClient.plugin.extensionDefinition.listExtensionDefinition(
            params
          ),
        {
          size: 1000,
          fieldSelector: [
            `spec.extensionPointName=${extensionPointDefinition.value?.metadata.name}`,
          ],
        }
      );
    },
    enabled: computed(() => !!extensionPointDefinition.value),
  });
}
