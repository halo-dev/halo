import type { EditorView } from "@/tiptap/pm";

/**
 * Resolves a ProseMirror node view DOM reference to its nearest HTML element.
 *
 * @remarks
 * ProseMirror node views may expose a non-element DOM node. In that case, this
 * helper returns its parent element.
 *
 * @param view - The ProseMirror editor view containing the node view.
 * @param pos - The document position at which the node starts.
 * @returns The node view element, its parent element, or `null` when no element
 *   can be resolved.
 *
 * @example
 * ```ts
 * import { getEditorNodeElement } from "@halo-dev/richtext-editor";
 *
 * const firstNodeElement = getEditorNodeElement(editor.view, 0);
 * firstNodeElement?.scrollIntoView({ block: "nearest" });
 * ```
 */
export function getEditorNodeElement(
  view: EditorView,
  pos: number
): HTMLElement | null {
  const nodeDOM = view.nodeDOM(pos);
  if (nodeDOM instanceof HTMLElement) {
    return nodeDOM;
  }
  if (nodeDOM?.parentElement instanceof HTMLElement) {
    return nodeDOM.parentElement;
  }
  return null;
}
