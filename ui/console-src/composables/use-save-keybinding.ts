import { useEventListener } from "@vueuse/core";
import { useDebounceFn } from "@vueuse/shared";
import { nextTick } from "vue";
import { isMac } from "@/utils/device";

export function useSaveKeybinding(fn: () => void) {
  const debouncedFn = useDebounceFn(() => {
    fn();
  }, 300);

  useEventListener(window, "keydown", (e: KeyboardEvent) => {
    if (isMac ? e.metaKey : e.ctrlKey) {
      if (e.key === "s") {
        e.preventDefault();
        nextTick(() => {
          debouncedFn();
        });
      }
    }
  });
}
