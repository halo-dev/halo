import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import type { Ref } from "vue";

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
      const { data } = await coreApiClient.storage.policy.listPolicy();
      return data.items;
    },
    refetchInterval(data) {
      const hasDeletingPolicy = data?.some(
        (policy) => !!policy.metadata.deletionTimestamp
      );
      return hasDeletingPolicy ? 1000 : false;
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
        await coreApiClient.storage.policyTemplate.listPolicyTemplate();
      return data.items;
    },
  });

  return {
    policyTemplates: data,
    isLoading,
    handleFetchPolicyTemplates: refetch,
  };
}
