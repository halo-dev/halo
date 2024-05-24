import { apiClient } from "@/utils/api-client";
import { watch, type Ref, ref, nextTick } from "vue";

interface SnapshotContent {
  version: Ref<number>;
  handleFetchSnapshot: () => Promise<void>;
}

export function useContentSnapshot(
  snapshotName: Ref<string | undefined>
): SnapshotContent {
  const version = ref(0);
  watch(snapshotName, () => {
    nextTick(() => {
      handleFetchSnapshot();
    });
  });

  const handleFetchSnapshot = async () => {
    if (!snapshotName.value) {
      return;
    }
    const { data } =
      await apiClient.extension.snapshot.getContentHaloRunV1alpha1Snapshot({
        name: snapshotName.value,
      });
    version.value = data.metadata.version || 0;
  };

  return {
    version,
    handleFetchSnapshot,
  };
}
