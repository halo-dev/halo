import type { EditorState } from "@/tiptap";
import { Fragment, type ResolvedPos } from "@/tiptap/pm";

/**
 * Creates the minimal schema-valid fragment needed for editable text at a
 * document position.
 *
 * @param state - The current ProseMirror editor state.
 * @param $pos - The resolved position where the fragment will be inserted.
 * @returns A schema-valid wrapping fragment, or `null` when the schema does not
 *   allow editable text at the position.
 *
 * @example
 * ```ts
 * import { createTextblockFragmentAt } from "@halo-dev/richtext-editor";
 *
 * const { state, view } = editor;
 * const $pos = state.selection.$from;
 * const fragment = createTextblockFragmentAt(state, $pos);
 *
 * if (fragment) {
 *   view.dispatch(state.tr.insert($pos.pos, fragment));
 * }
 * ```
 */
export function createTextblockFragmentAt(
  state: EditorState,
  $pos: ResolvedPos
): Fragment | null {
  const wrapping = $pos.parent
    .contentMatchAt($pos.index())
    .findWrapping(state.schema.nodes.text);
  if (!wrapping) {
    return null;
  }

  let fragment = Fragment.empty;
  for (let index = wrapping.length - 1; index >= 0; index--) {
    const node = wrapping[index].createAndFill(null, fragment);
    if (!node) {
      return null;
    }
    fragment = Fragment.from(node);
  }
  return fragment;
}
