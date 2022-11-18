import { onMounted, onUnmounted, ref, type Ref } from "vue";
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
  const refreshInterval = ref();

  const handleFetchGroups = async () => {
    try {
      clearInterval(refreshInterval.value);

      loading.value = true;
      const { data } =
        await apiClient.extension.storage.group.liststorageHaloRunV1alpha1Group();
      groups.value = data.items;

      const deletedGroups = groups.value.filter(
        (group) => !!group.metadata.deletionTimestamp
      );

      if (deletedGroups.length) {
        refreshInterval.value = setInterval(() => {
          handleFetchGroups();
        }, 1000);
      }
    } catch (e) {
      console.error("Failed to fetch attachment groups", e);
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    fetchOnMounted && handleFetchGroups();
  });

  onUnmounted(() => {
    clearInterval(refreshInterval.value);
  });

  return {
    groups,
    loading,
    handleFetchGroups,
  };
}
