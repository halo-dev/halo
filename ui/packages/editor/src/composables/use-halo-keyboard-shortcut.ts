import {
  computed,
  onMounted,
  onUnmounted,
  readonly,
  shallowRef,
  watch,
} from "vue";
import {
  getHaloKeyboardShortcut,
  subscribeHaloKeyboardShortcuts,
  type ResolvedHaloKeyboardShortcut,
} from "@/keyboard-shortcuts";
import type { Editor } from "@/tiptap";

/**
 * Reactively resolves one shortcut from an editor-scoped Halo shortcut
 * registry. Call this composable during component setup.
 */
export function useHaloKeyboardShortcut(
  editor: Editor,
  shortcutId: () => string | undefined
) {
  const shortcuts = useHaloKeyboardShortcuts(editor, () => {
    const id = shortcutId();
    if (!id) {
      return [];
    }
    return [id];
  });

  return computed(() => shortcuts.value[0]);
}

/**
 * Reactively resolves shortcuts from an editor-scoped Halo shortcut registry.
 * The result follows the order of the requested IDs.
 */
export function useHaloKeyboardShortcuts(
  editor: Editor,
  shortcutIds: () => string[]
) {
  const shortcuts = shallowRef<ResolvedHaloKeyboardShortcut[]>([]);
  let unsubscribe: (() => void) | undefined;

  function refreshShortcuts() {
    shortcuts.value = shortcutIds()
      .map((id) => getHaloKeyboardShortcut(editor, id))
      .filter((shortcut) => shortcut !== undefined);
  }

  watch(shortcutIds, refreshShortcuts, { immediate: true });

  onMounted(() => {
    // Registration and the first component render can happen in either order.
    unsubscribe = subscribeHaloKeyboardShortcuts(editor, refreshShortcuts);
    refreshShortcuts();
  });

  onUnmounted(() => unsubscribe?.());

  return readonly(shortcuts);
}
