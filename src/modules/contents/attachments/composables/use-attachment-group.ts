import { onMounted, ref, type Ref } from "vue";
import type { Group } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";

interface useFetchAttachmentGroupReturn {
  groups: Ref<Group[]>;
  loading: Ref<boolean>;
  handleFetchGroups: () => void;
}

export function useFetchAttachmentGroup(options?: {
  fetchOnMounted: boolean;
}): useFetchAttachmentGroupReturn {
  const { fetchOnMounted } = options || {};

  const groups = ref<Group[]>([] as Group[]);
  const loading = ref<boolean>(false);

  const handleFetchGroups = async () => {
    try {
      loading.value = true;
      const { data } =
        await apiClient.extension.storage.group.liststorageHaloRunV1alpha1Group();
      groups.value = data.items;
    } catch (e) {
      console.error("Failed to fetch attachment groups", e);
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    fetchOnMounted && handleFetchGroups();
  });

  return {
    groups,
    loading,
    handleFetchGroups,
  };
}
