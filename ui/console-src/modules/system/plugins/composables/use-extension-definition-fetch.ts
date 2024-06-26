import type { ExtensionPointDefinition } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import { computed, type Ref } from "vue";

export function useExtensionDefinitionFetch(
  extensionPointDefinition: Ref<ExtensionPointDefinition | undefined>
) {
  return useQuery({
    queryKey: ["extension-definitions", extensionPointDefinition],
    queryFn: async () => {
      const { data } =
        await coreApiClient.plugin.extensionDefinition.listExtensionDefinition({
          fieldSelector: [
            `spec.extensionPointName=${extensionPointDefinition.value?.metadata.name}`,
          ],
        });
      return data;
    },
    enabled: computed(() => !!extensionPointDefinition.value),
  });
}
