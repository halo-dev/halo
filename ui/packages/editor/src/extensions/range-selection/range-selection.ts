import {
  getSelectionRanges,
  NodeRangeSelection,
} from "@tiptap/extension-node-range";
import {
  Selection,
  type EditorState,
  type Node,
  type ResolvedPos,
} from "@/tiptap/pm";

/**
 * Compatibility layer for the former Halo RangeSelection.
 *
 * @deprecated Use NodeRangeSelection from @tiptap/extension-node-range.
 */
export class RangeSelection extends NodeRangeSelection {
  static [Symbol.hasInstance](instance: unknown) {
    return instance instanceof NodeRangeSelection;
  }

  static valid(state: EditorState, anchor: number, head: number) {
    if (anchor === head) {
      return false;
    }

    const $anchor = state.doc.resolve(anchor);
    const $head = state.doc.resolve(head);

    return (
      getSelectionRanges($anchor.min($head), $anchor.max($head)).length > 0
    );
  }

  static between($anchor: ResolvedPos, $head: ResolvedPos) {
    if ($anchor.pos === $head.pos) {
      return null;
    }

    const ranges = getSelectionRanges($anchor.min($head), $anchor.max($head));
    if (ranges.length === 0) {
      return null;
    }

    return new RangeSelection($anchor, $head);
  }

  static allRange(doc: Node) {
    return new RangeSelection(doc.resolve(0), doc.resolve(doc.content.size));
  }
}

try {
  Selection.jsonID("range", RangeSelection);
} catch {
  // The compatibility JSON ID may already be registered by another bundle.
}

export default RangeSelection;
