import {
  ResolvedPos,
  Selection,
  Node,
  type Mappable,
  EditorState,
} from "@/tiptap/pm";

/**
 * The RangeSelection class represents a selection range within a document.
 * The content can include text, paragraphs, block-level content, etc.
 *
 * It has a starting position and an ending position, and the nodes within the given range must be enclosed.
 * The RangeSelection must not contain empty content.
 */
class RangeSelection extends Selection {
  /**
   * Creates a RangeSelection between the specified positions.
   *
   * @param $anchor - The starting position of the selection.
   * @param $head - The ending position of the selection.
   */
  constructor($anchor: ResolvedPos, $head: ResolvedPos) {
    checkRangeSelection($anchor, $head);
    super($anchor, $head);
  }

  map(doc: Node, mapping: Mappable): Selection {
    const $head = doc.resolve(mapping.map(this.head));
    const $anchor = doc.resolve(mapping.map(this.anchor));
    return new RangeSelection($anchor, $head);
  }

  eq(other: Selection): boolean {
    return (
      other instanceof RangeSelection &&
      other.anchor == this.anchor &&
      other.head == this.head
    );
  }

  getBookmark() {
    return new RangeBookmark(this.anchor, this.head);
  }

  toJSON(): any {
    return { type: "range", anchor: this.anchor, head: this.head };
  }

  /**
   * Validates if the given positions can form a valid RangeSelection in the given state.
   *
   * @param state - The editor state.
   * @param anchor - The starting position.
   * @param head - The ending position.
   * @returns True if the positions form a valid RangeSelection, otherwise false.
   */
  static valid(state: EditorState, anchor: number, head: number) {
    const { doc } = state;
    if (anchor === head) {
      return false;
    }
    const dir = anchor < head ? 1 : -1;
    const anchorPos = dir > 0 ? anchor : head;
    const headPos = dir > 0 ? head : anchor;
    let isRangeSelection = false;
    doc.nodesBetween(anchorPos, headPos, (node, pos) => {
      if (node.isText || node.type.name === "paragraph" || isRangeSelection) {
        return false;
      }
      if (pos + 1 < anchorPos || pos + 1 > headPos) {
        return false;
      }
      if (node.type.spec.fakeSelection) {
        isRangeSelection = true;
        return false;
      }
    });
    return isRangeSelection;
  }

  static fromJSON(doc: Node, json: any) {
    if (typeof json.anchor != "number" || typeof json.head != "number") {
      throw new RangeError("Invalid input for RangeSelection.fromJSON");
    }

    return new RangeSelection(doc.resolve(json.anchor), doc.resolve(json.head));
  }

  static create(doc: Node, anchor: number, head: number) {
    return new this(doc.resolve(anchor), doc.resolve(head));
  }

  static allRange(doc: Node) {
    return new RangeSelection(doc.resolve(0), doc.resolve(doc.content.size));
  }
}

Selection.jsonID("range", RangeSelection);

class RangeBookmark {
  constructor(readonly anchor: number, readonly head: number) {}

  map(mapping: Mappable) {
    return new RangeBookmark(mapping.map(this.anchor), mapping.map(this.head));
  }
  resolve(doc: Node) {
    return new RangeSelection(doc.resolve(this.anchor), doc.resolve(this.head));
  }
}

export function checkRangeSelection($anchor: ResolvedPos, $head: ResolvedPos) {
  if ($anchor.pos === $head.pos) {
    console.warn("The RangeSelection cannot be empty.");
  }
}

export default RangeSelection;
