import {
  EditorState,
  Node,
  ResolvedPos,
  Selection,
  type Mappable,
} from "@/tiptap/pm";

/**
 * The RangeSelection class represents a selection range within a document.
 * The content can include text, paragraphs, block-level content, etc.
 *
 * It has a starting position and an ending position. When the given range includes block-level content,
 * the RangeSelection will automatically expand to include the block-level content at the corresponding depth.
 *
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
    const nodes = rangeNodesBetween(
      state.doc.resolve(anchor),
      state.doc.resolve(head)
    );

    if (nodes.length === 0) {
      return false;
    }

    if (nodes.reverse()[0].pos < 0) {
      return false;
    }

    return true;
  }

  /**
   * Returns a RangeSelection spanning the given positions.
   *
   * When the given range includes block-level content, if only a part is included,
   * the selection will be expanded to encompass the block-level content at the corresponding depth.
   *
   * Expansion: If the selection includes all depth nodes of the current block-level content but not the entire last node,
   * the selection will be expanded to include the node at that depth.
   *
   * @param $anchor - The starting position of the selection.
   * @param $head - The ending position of the selection.
   * @returns A new RangeSelection that spans the given positions.
   */
  static between($anchor: ResolvedPos, $head: ResolvedPos) {
    checkRangeSelection($anchor, $head);

    const doc = $anchor.doc;
    const dir = $anchor.pos < $head.pos ? 1 : -1;
    const anchorPos = dir > 0 ? $anchor.pos : $head.pos;
    const headPos = dir > 0 ? $head.pos : $anchor.pos;
    const nodes = rangeNodesBetween($anchor, $head);

    if (nodes.length === 0) {
      return null;
    }

    const lastNode = nodes[nodes.length - 1];
    if (lastNode.pos < 0) {
      return null;
    }

    let fromOffset = 0;
    nodes.forEach(({ pos }) => {
      if (pos < 0) {
        fromOffset = pos;
      }
    });

    const toOffset =
      headPos - anchorPos - lastNode.pos - lastNode.node.nodeSize;
    const anchor =
      dir > 0
        ? anchorPos + fromOffset
        : headPos - (toOffset > 0 ? 0 : toOffset);
    const head =
      dir > 0
        ? headPos - (toOffset > 0 ? 0 : toOffset)
        : anchorPos + fromOffset;
    return new RangeSelection(doc.resolve(anchor), doc.resolve(head));
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

export function rangeNodesBetween($anchor: ResolvedPos, $head: ResolvedPos) {
  const doc = $anchor.doc;
  const dir = $anchor.pos < $head.pos ? 1 : -1;
  const anchorPos = dir > 0 ? $anchor.pos : $head.pos;
  const headPos = dir > 0 ? $head.pos : $anchor.pos;

  const nodes: Array<{
    node: Node;
    pos: number;
    parent: Node | null;
    index: number;
  }> = [];
  doc.nodesBetween(
    anchorPos,
    headPos,
    (node, pos, parent, index) => {
      if (node.isText || node.type.name === "paragraph") {
        return true;
      }
      nodes.push({ node, pos, parent, index });
    },
    -anchorPos
  );
  return nodes;
}

export default RangeSelection;
