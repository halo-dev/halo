import type { Ref } from "vue";
import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { useQuery } from "@tanstack/vue-query";

interface useFetchAttachmentPolicyReturn {
  policies: Ref<Policy[] | undefined>;
  isLoading: Ref<boolean>;
  handleFetchPolicies: () => void;
}

interface useFetchAttachmentPolicyTemplatesReturn {
  policyTemplates: Ref<PolicyTemplate[] | undefined>;
  isLoading: Ref<boolean>;
  handleFetchPolicyTemplates: () => void;
}

export function useFetchAttachmentPolicy(): useFetchAttachmentPolicyReturn {
  const { data, isLoading, refetch } = useQuery<Policy[]>({
    queryKey: ["attachment-policies"],
    queryFn: async () => {
      const { data } =
        await apiClient.extension.storage.policy.liststorageHaloRunV1alpha1Policy();
      return data.items;
    },
    refetchInterval(data) {
      const deletingPolicies = data?.filter(
        (policy) => !!policy.metadata.deletionTimestamp
      );
      return deletingPolicies?.length ? 1000 : false;
    },
  });

  return {
    policies: data,
    isLoading,
    handleFetchPolicies: refetch,
  };
}

export function useFetchAttachmentPolicyTemplate(): useFetchAttachmentPolicyTemplatesReturn {
  const { data, isLoading, refetch } = useQuery<PolicyTemplate[]>({
    queryKey: ["attachment-policy-templates"],
    queryFn: async () => {
      const { data } =
        await apiClient.extension.storage.policyTemplate.liststorageHaloRunV1alpha1PolicyTemplate();
      return data.items;
    },
  });

  return {
    policyTemplates: data,
    isLoading,
    handleFetchPolicyTemplates: refetch,
  };
}
