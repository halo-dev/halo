import type { PMNode } from "@/tiptap";
import {
  NodeSelection,
  ResolvedPos,
  Selection,
  Slice,
  type Mappable,
} from "@/tiptap/pm";

class GapCursorSelection extends Selection {
  private start: boolean | null = false;

  constructor($pos: ResolvedPos) {
    super($pos, $pos);
    this.start = isNodeStart($pos);
  }

  map(doc: PMNode, mapping: Mappable): Selection {
    const $pos = doc.resolve(mapping.map(this.head));
    return GapCursorSelection.valid($pos)
      ? new GapCursorSelection($pos)
      : Selection.near($pos);
  }

  content() {
    return Slice.empty;
  }

  eq(other: Selection): boolean {
    return other instanceof GapCursorSelection && other.head == this.head;
  }

  toJSON(): any {
    return { type: "node-gap-cursor", pos: this.head };
  }

  get isStart() {
    return this.start;
  }

  static fromJSON(doc: PMNode, json: any): GapCursorSelection {
    if (typeof json.pos != "number") {
      throw new RangeError("Invalid input for GapCursorSelection.fromJSON");
    }
    return new GapCursorSelection(doc.resolve(json.pos));
  }

  getBookmark() {
    return new GapBookmark(this.anchor);
  }

  /**
   * Validates if a GapCursor can be placed at the given position
   *
   * This function checks whether a GapCursor can be placed at the specified position in the document.
   * It ensures that the position is not within a text block, and that the node at the position allows a GapCursor.
   *
   * @param {ResolvedPos} $pos - The resolved position in the document to validate.
   * @returns {boolean} - Returns true if a GapCursor can be placed at the given position, false otherwise.
   */
  static valid($pos: ResolvedPos) {
    if ($pos.depth < 1) {
      return false;
    }
    // Get the node at the current position
    const nodeOffset = $pos.doc.childBefore($pos.pos);
    const root = nodeOffset.node;
    if (!root) {
      return false;
    }
    const parent = $pos.parent;
    if (parent.isTextblock || (!closedBefore($pos) && !closedAfter($pos))) {
      return false;
    }
    // Check if the node allows a GapCursor
    const override = root.type.spec.allowGapCursor;
    if (!override) {
      return false;
    }
    return !root.type.inlineContent;
  }

  static findGapCursorFrom($pos: ResolvedPos, dir: number, mustMove = false) {
    let keepSearching = true;
    while (keepSearching) {
      if (!mustMove && GapCursorSelection.valid($pos)) {
        return $pos;
      }
      let pos = $pos.pos;
      let next: PMNode | null = null;

      // Scan up from this position
      for (let d = $pos.depth; d >= 0; d--) {
        const parent = $pos.node(d);
        const index = dir > 0 ? $pos.indexAfter(d) : $pos.index(d) - 1;

        if (dir > 0 ? index < parent.childCount : index >= 0) {
          next = parent.child(index);
          break;
        }

        if (d == 0) {
          return null;
        }

        pos += dir;
        const $cur = $pos.doc.resolve(pos);
        if (GapCursorSelection.valid($cur)) {
          return $cur;
        }
      }

      // And then down into the next node
      while (next) {
        const inside = dir > 0 ? next.firstChild : next.lastChild;
        if (!inside) {
          if (
            next.isAtom &&
            !next.isText &&
            !NodeSelection.isSelectable(next)
          ) {
            $pos = $pos.doc.resolve(pos + next.nodeSize * dir);
            mustMove = false;
            break;
          }
          keepSearching = false;
          break;
        }
        next = inside;
        pos += dir;
        const $cur = $pos.doc.resolve(pos);
        if (GapCursorSelection.valid($cur)) {
          return $cur;
        }
      }

      if (!next) {
        keepSearching = false;
      }
    }

    return null;
  }
}

GapCursorSelection.prototype.visible = false;
(GapCursorSelection as any).findFrom = GapCursorSelection.findGapCursorFrom;

Selection.jsonID("node-gap-cursor", GapCursorSelection);

class GapBookmark {
  constructor(readonly pos: number) {}

  map(mapping: Mappable) {
    return new GapBookmark(mapping.map(this.pos));
  }

  resolve(doc: PMNode) {
    const $pos = doc.resolve(this.pos);
    return GapCursorSelection.valid($pos)
      ? new GapCursorSelection($pos)
      : Selection.near($pos);
  }
}

/**
 * Checks if the position before the given resolved position is closed
 *
 * This function traverses up the document tree from the given resolved position and checks if the position
 * immediately before it is closed. A position is considered closed if the previous node is closed or
 * if the parent node is isolating.
 *
 * @param {ResolvedPos} $pos - The resolved position in the document to check.
 * @returns {boolean} - Returns true if the position before the given position is closed, false otherwise.
 */
export function closedBefore($pos: ResolvedPos) {
  for (let d = $pos.depth; d >= 0; d--) {
    const index = $pos.index(d);
    const parent = $pos.node(d);

    if (index === 0) {
      if (parent.type.spec.isolating) {
        return true;
      }
      continue;
    }

    if (isNodeClosed(parent.child(index - 1), false)) {
      return true;
    }
  }
  return true;
}

export function closedAfter($pos: ResolvedPos) {
  for (let d = $pos.depth; d >= 0; d--) {
    const index = $pos.indexAfter(d);
    const parent = $pos.node(d);

    if (index === parent.childCount) {
      if (parent.type.spec.isolating) {
        return true;
      }
      continue;
    }

    if (isNodeClosed(parent.child(index), true)) {
      return true;
    }
  }
  return true;
}

function isNodeClosed(node: PMNode, isAfter: boolean): boolean {
  while (node) {
    if (
      (node.childCount === 0 && !node.inlineContent) ||
      node.isAtom ||
      node.type.spec.isolating
    ) {
      return true;
    }
    if (node.inlineContent) {
      return false;
    }
    node = (isAfter ? node.firstChild : node.lastChild) as PMNode;
  }
  return false;
}

export function isNodeStart($pos: ResolvedPos) {
  if ($pos.depth < 1) {
    return null;
  }
  const startPos = $pos.start(1);
  const endPos = $pos.end(1);
  return $pos.pos < startPos + (endPos - startPos) / 2;
}

export default GapCursorSelection;
