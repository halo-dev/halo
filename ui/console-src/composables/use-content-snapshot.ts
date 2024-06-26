import { coreApiClient } from "@halo-dev/api-client";
import { nextTick, ref, watch, type Ref } from "vue";

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
    const { data } = await coreApiClient.content.snapshot.getSnapshot({
      name: snapshotName.value,
    });
    version.value = data.metadata.version || 0;
  };

  return {
    version,
    handleFetchSnapshot,
  };
}
