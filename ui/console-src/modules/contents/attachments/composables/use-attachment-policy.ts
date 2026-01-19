import { attachmentPolicyLabels } from "@/constants/labels";
import { paginate } from "@/utils/paginate";
import type {
  Policy,
  PolicyTemplate,
  PolicyTemplateV1alpha1ApiListPolicyTemplateRequest,
  PolicyV1alpha1ApiListPolicyRequest,
} from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";

export function useFetchAttachmentPolicy() {
  return useQuery<Policy[]>({
    queryKey: ["attachment-policies"],
    queryFn: async () => {
      const policies = await paginate<
        PolicyV1alpha1ApiListPolicyRequest,
        Policy
      >((params) => coreApiClient.storage.policy.listPolicy(params), {
        size: 1000,
      });
      return policies.sort((a, b) => {
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
      return await paginate<
        PolicyTemplateV1alpha1ApiListPolicyTemplateRequest,
        PolicyTemplate
      >(
        (params) =>
          coreApiClient.storage.policyTemplate.listPolicyTemplate(params),
        {
          size: 1000,
        }
      );
    },
  });
}
