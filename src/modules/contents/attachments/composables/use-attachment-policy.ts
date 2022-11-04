import { onMounted, ref } from "vue";
import type { Ref } from "vue";
import type { Policy, PolicyTemplate } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";

interface useFetchAttachmentPolicyReturn {
  policies: Ref<Policy[]>;
  loading: Ref<boolean>;
  handleFetchPolicies: () => void;
}

interface useFetchAttachmentPolicyTemplatesReturn {
  policyTemplates: Ref<PolicyTemplate[]>;
  loading: Ref<boolean>;
  handleFetchPolicyTemplates: () => void;
}

export function useFetchAttachmentPolicy(options?: {
  fetchOnMounted: boolean;
}): useFetchAttachmentPolicyReturn {
  const { fetchOnMounted } = options || {};

  const policies = ref<Policy[]>([] as Policy[]);
  const loading = ref<boolean>(false);

  const handleFetchPolicies = async () => {
    try {
      loading.value = true;
      const { data } =
        await apiClient.extension.storage.policy.liststorageHaloRunV1alpha1Policy();
      policies.value = data.items;
    } catch (e) {
      console.error("Failed to fetch attachment policies", e);
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    fetchOnMounted && handleFetchPolicies();
  });

  return {
    policies,
    loading,
    handleFetchPolicies,
  };
}

export function useFetchAttachmentPolicyTemplate(options?: {
  fetchOnMounted: boolean;
}): useFetchAttachmentPolicyTemplatesReturn {
  const { fetchOnMounted } = options || {};

  const policyTemplates = ref<PolicyTemplate[]>([] as PolicyTemplate[]);
  const loading = ref<boolean>(false);

  const handleFetchPolicyTemplates = async () => {
    try {
      loading.value = true;
      const { data } =
        await apiClient.extension.storage.policyTemplate.liststorageHaloRunV1alpha1PolicyTemplate();
      policyTemplates.value = data.items;
    } catch (e) {
      console.error("Failed to fetch attachment policy templates", e);
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    fetchOnMounted && handleFetchPolicyTemplates();
  });

  return {
    policyTemplates,
    loading,
    handleFetchPolicyTemplates,
  };
}
