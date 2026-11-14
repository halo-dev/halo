import {
  clampIndentLevel,
  getHaloEditorIndentationSettings,
} from "@/editor-metadata/indentation";
import { liftListItem, type Editor, type PMNode, type Range } from "@/tiptap";
import {
  Selection,
  type EditorState,
  type ResolvedPos,
  type Transaction,
} from "@/tiptap/pm";

/** A list item containing a resolved document position. */
export interface AncestorListItem {
  /** The list-item node. */
  node: PMNode;

  /** The absolute position before the list-item node. */
  pos: number;

  /** The list-item node depth in the resolved position. */
  depth: number;
}

/**
 * Finds list-item ancestors by schema group rather than by node name.
 *
 * @remarks
 * Results are ordered from the innermost item to the outermost item. A node is
 * considered a list item when its parent belongs to the `list` schema group,
 * so this helper also supports third-party list node names.
 *
 * @param $pos - The resolved document position to inspect.
 * @returns All ancestor list items containing the position.
 *
 * @example
 * ```ts
 * import { findAncestorListItems } from "@halo-dev/richtext-editor";
 *
 * const items = findAncestorListItems(editor.state.selection.$from);
 * const activeListItem = items[0];
 * ```
 */
export function findAncestorListItems($pos: ResolvedPos): AncestorListItem[] {
  const items: AncestorListItem[] = [];
  for (let depth = $pos.depth; depth > 0; depth--) {
    if (!$pos.node(depth - 1).type.isInGroup("list")) {
      continue;
    }
    items.push({
      node: $pos.node(depth),
      pos: $pos.before(depth),
      depth,
    });
  }
  return items;
}

/**
 * Resolves the visual block indentation inherited by the current selection.
 *
 * @remarks
 * Explicit indentation on the active text block takes precedence when it is
 * deeper than the surrounding list. Each ancestor list contributes one
 * configured indentation step, and the result is clamped to the editor's
 * configured indentation bounds.
 *
 * @param editor - The Tiptap editor whose selection should be inspected.
 * @returns The indentation value in pixels.
 *
 * @example
 * ```ts
 * import { getBlockIndentAtSelection } from "@halo-dev/richtext-editor";
 *
 * const indent = getBlockIndentAtSelection(editor);
 * editor.commands.setNode("myBlock", { indent });
 * ```
 */
export function getBlockIndentAtSelection(editor: Editor): number {
  const { $from } = editor.state.selection;
  const settings = getHaloEditorIndentationSettings(editor);
  const listIndent = findAncestorListItems($from).length * settings.indentRange;
  return clampIndentLevel(
    Math.max(Number($from.parent.attrs.indent) || 0, listIndent),
    settings
  );
}

/**
 * Moves a block-producing command out of the active list while preserving its
 * visual indentation.
 *
 * @remarks
 * This helper is intended for Slash Command-style integrations that replace a
 * text range with a block node. It deletes the trigger range, lifts through
 * all ancestor list items in one undoable transaction, and returns the mapped
 * collapsed range for the block command.
 *
 * @param editor - The Tiptap editor executing the command.
 * @param range - The trigger text range that the command will replace.
 * @returns The original range outside a list, or the mapped collapsed range
 * after lifting from a list.
 *
 * @example
 * ```ts
 * import { prepareBlockCommandFromList } from "@halo-dev/richtext-editor";
 *
 * const preparedRange = prepareBlockCommandFromList(editor, range);
 * editor.chain().deleteRange(preparedRange).setNode("myBlock").run();
 * ```
 */
export function prepareBlockCommandFromList(editor: Editor, range: Range) {
  const { $from } = editor.state.selection;
  const listItems = findAncestorListItems($from);
  if (listItems.length === 0 || !$from.parent.isTextblock) {
    return range;
  }

  const indent = getBlockIndentAtSelection(editor);
  const textblockName = $from.parent.type.name;
  editor.commands.focus();
  const transactions: Transaction[] = [];
  let state = applyTransaction(
    editor.state,
    editor.state.tr.delete(range.from, range.to),
    transactions
  );

  for (const { node } of listItems) {
    let liftTransaction: Transaction | undefined;
    liftListItem(node.type)(state, (transaction) => {
      liftTransaction = transaction;
    });
    if (liftTransaction) {
      state = applyTransaction(state, liftTransaction, transactions);
    }
  }

  const liftedPosition = state.selection.$from;
  if (
    liftedPosition.parent.type.name === textblockName &&
    liftedPosition.depth > 0
  ) {
    const pos = liftedPosition.before(liftedPosition.depth);
    const node = state.doc.nodeAt(pos);
    if (node) {
      state = applyTransaction(
        state,
        state.tr.setNodeMarkup(
          pos,
          node.type,
          { ...node.attrs, indent, lineIndent: false },
          node.marks
        ),
        transactions
      );
    }
  }

  const tr = editor.state.tr;
  for (const transaction of transactions) {
    for (const step of transaction.steps) {
      tr.step(step);
    }
  }
  tr.setSelection(Selection.fromJSON(tr.doc, state.selection.toJSON()));
  editor.view.dispatch(tr.scrollIntoView());

  const pos = editor.state.selection.from;
  return { from: pos, to: pos };
}

function applyTransaction(
  state: EditorState,
  transaction: Transaction,
  transactions: Transaction[]
) {
  transactions.push(transaction);
  return state.apply(transaction);
}
