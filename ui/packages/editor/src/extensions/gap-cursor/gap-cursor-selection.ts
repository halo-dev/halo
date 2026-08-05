import type { PMNode } from "@/tiptap";
import {
  GapCursor,
  Selection,
  type Mappable,
  type ResolvedPos,
  type SelectionBookmark,
} from "@/tiptap/pm";
import {
  isGapCursorPosition,
  resolveGapCursorSide,
  type GapCursorSide,
} from "@/utils";

/**
 * The GapCursor selection with an explicit visual side for positions
 * shared by two adjacent structural blocks.
 */
export class HaloGapCursor extends GapCursor {
  readonly side: GapCursorSide;

  constructor($pos: ResolvedPos, preferredSide?: GapCursorSide) {
    super($pos);
    this.side = resolveGapCursorSide($pos, preferredSide) ?? "after";
  }

  map(doc: PMNode, mapping: Mappable): Selection {
    const $pos = doc.resolve(mapping.map(this.head));
    if (!isGapCursorPosition($pos)) {
      return Selection.near($pos);
    }
    return new HaloGapCursor($pos, this.side);
  }

  eq(other: Selection): boolean {
    if (!(other instanceof HaloGapCursor)) {
      return false;
    }
    if (other.head !== this.head) {
      return false;
    }
    return other.side === this.side;
  }

  getBookmark(): SelectionBookmark {
    return new HaloGapBookmark(this.anchor, this.side);
  }
}

export function getGapCursorSelectionSide(
  selection: Selection
): GapCursorSide | null {
  const preferredSide =
    selection instanceof HaloGapCursor ? selection.side : undefined;
  return resolveGapCursorSide(selection.$from, preferredSide);
}

class HaloGapBookmark implements SelectionBookmark {
  constructor(
    private readonly pos: number,
    private readonly side: GapCursorSide
  ) {}

  map(mapping: Mappable) {
    return new HaloGapBookmark(mapping.map(this.pos), this.side);
  }

  resolve(doc: PMNode): Selection {
    const $pos = doc.resolve(this.pos);
    if (!isGapCursorPosition($pos)) {
      return Selection.near($pos);
    }
    return new HaloGapCursor($pos, this.side);
  }
}
