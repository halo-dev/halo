import { attachmentPolicyLabels } from "@/constants/labels";
import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";

export function useFetchAttachmentPolicy() {
  return useQuery<Policy[]>({
    queryKey: ["attachment-policies"],
    queryFn: async () => {
      const { data } = await coreApiClient.storage.policy.listPolicy();
      return data.items.sort((a, b) => {
        const priorityA = parseInt(
          a.metadata.labels?.[attachmentPolicyLabels.PRIORITY] || "0",
          10
        );
        const priorityB = parseInt(
          b.metadata.labels?.[attachmentPolicyLabels.PRIORITY] || "0",
          10
        );
        return priorityB - priorityA;
      });
    },
    refetchInterval(data) {
      const hasDeletingPolicy = data?.some(
        (policy) => !!policy.metadata.deletionTimestamp
      );
      return hasDeletingPolicy ? 1000 : false;
    },
  });
}

export function useFetchAttachmentPolicyTemplate() {
  return useQuery<PolicyTemplate[]>({
    queryKey: ["attachment-policy-templates"],
    queryFn: async () => {
      const { data } =
        await coreApiClient.storage.policyTemplate.listPolicyTemplate();
      return data.items;
    },
  });
}
