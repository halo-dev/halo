import type { PMNode } from "@/tiptap";
import { GapCursor, NodeSelection, type ResolvedPos } from "@/tiptap/pm";

/** The visual side of a structural block represented by a gap cursor. */
export type GapCursorSide = "before" | "after";

/** The document direction used when searching for a gap cursor position. */
export type GapCursorDirection = -1 | 1;

/** A structural block associated with a gap cursor position. */
export interface GapCursorTarget {
  /** The structural block adjacent to the gap cursor. */
  node: PMNode;

  /** The document position at which the structural block starts. */
  pos: number;
}

/**
 * Checks whether a node exposes gap cursor positions before and after itself.
 *
 * @remarks
 * A node's `createGapCursor` specification is treated as an explicit override.
 * Without an override, atom, isolating, and code blocks are recognized. Generic
 * flow containers such as lists and blockquotes are excluded.
 *
 * @param node - The ProseMirror node to inspect.
 * @returns `true` when the node supports Halo's before/after gap cursor.
 *
 * @example
 * ```ts
 * import { isGapCursorTargetNode } from "@halo-dev/richtext-editor";
 *
 * const firstNode = editor.state.doc.firstChild;
 * if (isGapCursorTargetNode(firstNode)) {
 *   console.log("The first node supports a gap cursor.");
 * }
 * ```
 */
export function isGapCursorTargetNode(
  node: PMNode | null | undefined
): boolean {
  if (!node) {
    return false;
  }
  if (!node.isBlock) {
    return false;
  }
  if (node.isText) {
    return false;
  }

  const configured = node.type.spec.createGapCursor;
  if (configured != null) {
    return configured;
  }

  if (node.isAtom) {
    return true;
  }
  if (node.type.spec.isolating) {
    return true;
  }
  return Boolean(node.type.spec.code);
}

/**
 * Resolves which side of a structural block a document position represents.
 *
 * @param $pos - The resolved document position to inspect.
 * @param preferredSide - The side to prefer when two structural blocks share
 *   the same document position.
 * @returns The resolved side, or `null` when the position is not adjacent to a
 *   supported structural block.
 *
 * @example
 * ```ts
 * import { resolveGapCursorSide } from "@halo-dev/richtext-editor";
 *
 * const side = resolveGapCursorSide(editor.state.selection.$from, "before");
 * if (side) {
 *   console.log(`The gap cursor is on the ${side} side of the block.`);
 * }
 * ```
 */
export function resolveGapCursorSide(
  $pos: ResolvedPos,
  preferredSide?: GapCursorSide
): GapCursorSide | null {
  const canBeAfter = isGapCursorTargetNode($pos.nodeBefore);
  const canBeBefore = isGapCursorTargetNode($pos.nodeAfter);

  if (preferredSide === "after" && canBeAfter) {
    return "after";
  }
  if (preferredSide === "before" && canBeBefore) {
    return "before";
  }
  if (canBeAfter) {
    return "after";
  }
  if (canBeBefore) {
    return "before";
  }
  return null;
}

/**
 * Checks whether a resolved position can host a Halo gap cursor.
 *
 * @remarks
 * Nested positions follow ProseMirror's gap cursor rules. At the document
 * level, positions adjacent to a node recognized by
 * {@link isGapCursorTargetNode} are also supported.
 *
 * @param $pos - The resolved document position to inspect.
 * @returns `true` when a Halo gap cursor can be placed at the position.
 *
 * @example
 * ```ts
 * import { isGapCursorPosition } from "@halo-dev/richtext-editor";
 *
 * const { $from } = editor.state.selection;
 * if (isGapCursorPosition($from)) {
 *   console.log("The current selection is at a valid gap position.");
 * }
 * ```
 */
export function isGapCursorPosition($pos: ResolvedPos): boolean {
  if ($pos.parent.inlineContent) {
    return false;
  }

  if ($pos.depth !== 0) {
    return officialGapCursorValid($pos);
  }

  if (isGapCursorTargetNode($pos.nodeBefore)) {
    return true;
  }
  return isGapCursorTargetNode($pos.nodeAfter);
}

/**
 * Gets the structural block associated with one side of a gap position.
 *
 * @param $pos - The resolved gap position.
 * @param side - The visual side whose adjacent structural block should be
 *   returned.
 * @returns The structural block and its start position, or `null` when the
 *   requested side has no supported structural block.
 *
 * @example
 * ```ts
 * import {
 *   getGapCursorTarget,
 *   resolveGapCursorSide,
 * } from "@halo-dev/richtext-editor";
 *
 * const { $from } = editor.state.selection;
 * const side = resolveGapCursorSide($from);
 * const target = side ? getGapCursorTarget($from, side) : null;
 *
 * if (target) {
 *   console.log(target.node.type.name, target.pos);
 * }
 * ```
 */
export function getGapCursorTarget(
  $pos: ResolvedPos,
  side: GapCursorSide
): GapCursorTarget | null {
  if (side === "before") {
    const node = $pos.nodeAfter;
    if (!node) {
      return null;
    }
    if (!isGapCursorTargetNode(node)) {
      return null;
    }
    return { node, pos: $pos.pos };
  }

  const node = $pos.nodeBefore;
  if (!node) {
    return null;
  }
  if (!isGapCursorTargetNode(node)) {
    return null;
  }
  return { node, pos: $pos.pos - node.nodeSize };
}

/**
 * Finds a Halo-compatible gap cursor position in a document direction.
 *
 * @param $pos - The resolved position from which to start the search.
 * @param direction - `-1` to search backward or `1` to search forward.
 * @param mustMove - Whether to skip `$pos` even when it is already a valid gap
 *   position. Defaults to `false`.
 * @returns The matching resolved position, or `null` when no position exists in
 *   the requested direction.
 *
 * @example
 * ```ts
 * import {
 *   findGapCursorFrom,
 *   HaloGapCursor,
 * } from "@halo-dev/richtext-editor";
 *
 * const $next = findGapCursorFrom(editor.state.selection.$from, 1, true);
 * if ($next) {
 *   const selection = new HaloGapCursor($next);
 *   editor.view.dispatch(editor.state.tr.setSelection(selection));
 * }
 * ```
 */
export function findGapCursorFrom(
  $pos: ResolvedPos,
  direction: GapCursorDirection,
  mustMove = false
): ResolvedPos | null {
  search: for (;;) {
    if (!mustMove && isGapCursorPosition($pos)) {
      return $pos;
    }

    let pos = $pos.pos;
    let next: PMNode | null = null;

    for (let depth = $pos.depth; ; depth--) {
      const parent = $pos.node(depth);
      const index =
        direction > 0 ? $pos.indexAfter(depth) : $pos.index(depth) - 1;
      const hasSibling = direction > 0 ? index < parent.childCount : index >= 0;

      if (hasSibling) {
        next = parent.child(index);
        break;
      }
      if (depth === 0) {
        return null;
      }

      pos += direction;
      const $current = $pos.doc.resolve(pos);
      if (isGapCursorPosition($current)) {
        return $current;
      }
    }

    if (!next) {
      return null;
    }

    if (isGapCursorTargetNode(next)) {
      const $outside = $pos.doc.resolve(pos + next.nodeSize * direction);
      if (isGapCursorPosition($outside)) {
        return $outside;
      }
    }

    for (;;) {
      const current: PMNode = next;
      const inside = direction > 0 ? current.firstChild : current.lastChild;

      if (!inside) {
        const $outside = $pos.doc.resolve(pos + current.nodeSize * direction);
        if (isGapCursorPosition($outside)) {
          return $outside;
        }

        const isUnselectableAtom =
          current.isAtom &&
          !current.isText &&
          !NodeSelection.isSelectable(current);
        if (isUnselectableAtom) {
          $pos = $outside;
          mustMove = false;
          continue search;
        }
        break;
      }

      next = inside;
      pos += direction;
      const $current = $pos.doc.resolve(pos);
      if (isGapCursorPosition($current)) {
        return $current;
      }
    }

    return null;
  }
}

function officialGapCursorValid($pos: ResolvedPos) {
  return (
    GapCursor as unknown as { valid: (pos: ResolvedPos) => boolean }
  ).valid($pos);
}
