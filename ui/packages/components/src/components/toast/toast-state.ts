import { reactive } from "vue";

export const DEFAULT_TOAST_DURATION = 3000;
export const TOAST_MAX_COUNT = 5;
export const TOAST_HOTKEY = ["F8"];

/**
 * Toasts stay mounted for one animation frame window after they close so the
 * leave animation can run. Reka UI's `Presence` cannot observe the animation
 * of `ToastRoot` (it renders a fragment root), so we keep the item force
 * mounted for `LEAVE_MS` and let it unmount afterwards.
 */
const LEAVE_MS = 200;

export const closingIds = reactive(new Set<string>());

const closingTimers = new Map<string, ReturnType<typeof setTimeout>>();

function prefersReducedMotion() {
  return (
    typeof window !== "undefined" &&
    window.matchMedia?.("(prefers-reduced-motion: reduce)").matches === true
  );
}

export function markToastClosing(id: string) {
  closingIds.add(id);

  const pending = closingTimers.get(id);
  if (pending !== undefined) {
    clearTimeout(pending);
  }

  closingTimers.set(
    id,
    setTimeout(
      () => {
        closingIds.delete(id);
        closingTimers.delete(id);
      },
      prefersReducedMotion() ? 0 : LEAVE_MS
    )
  );
}

export function cancelToastClosing(id: string) {
  closingIds.delete(id);

  const pending = closingTimers.get(id);
  if (pending !== undefined) {
    clearTimeout(pending);
    closingTimers.delete(id);
  }
}
