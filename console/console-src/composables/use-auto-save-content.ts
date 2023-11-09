import { useWindowFocus } from "@vueuse/core";
import { watch, type Ref } from "vue";
import { onBeforeRouteLeave } from "vue-router";
import { useTimeoutFn } from "@vueuse/core";
import type { ContentCache } from "./use-content-cache";

export function useAutoSaveContent(
  currentCache: Ref<ContentCache | undefined>,
  raw: Ref<string | undefined>,
  callback: () => void
) {
  // TODO it may be necessary to know the latest version before saving, otherwise it will overwrite the latest version
  const handleAutoSave = () => {
    if (currentCache.value) {
      callback();
    }
  };

  onBeforeRouteLeave(() => {
    handleAutoSave();
  });

  watch(useWindowFocus(), (newFocus) => {
    if (!newFocus) {
      handleAutoSave();
    }
  });

  window.addEventListener("beforeunload", () => {
    handleAutoSave();
  });

  const { start, isPending, stop } = useTimeoutFn(() => {
    handleAutoSave();
  }, 20 * 1000);
  watch(raw, () => {
    if (isPending.value) {
      stop();
    }
    start();
  });
}
