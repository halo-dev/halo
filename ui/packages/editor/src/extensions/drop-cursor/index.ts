import { Dropcursor, type DropcursorOptions } from "@tiptap/extensions";
import {
  clampIndentLevel,
  DROP_INDENT_TRANSACTION_META,
  getHaloEditorIndentationSettings,
  indentLevelToValue,
  isNodeIndentable,
  resolveHaloEditorIndentationSettings,
  type HaloEditorIndentationSettings,
} from "@/editor-metadata/indentation";
import {
  Decoration,
  DecorationSet,
  dropPoint,
  NodeSelection,
  Plugin,
  PluginKey,
  type Slice,
  type EditorState,
  type EditorView,
} from "@/tiptap/pm";
import { findAncestorListItems } from "@/utils";

interface IndentDropPreview {
  pos: number;
  level: number;
  indentRange: number;
  minIndentLevel: number;
  maxIndentLevel: number;
  normalizedListDrop?: boolean;
  listItemDrop?: {
    insertPos: number;
    depth: number;
    anchorLeft: number;
    anchorRight: number;
    anchorTop: number;
  };
  sourceRange?: { from: number; to: number };
}

interface NormalizedListDrop {
  pos: number;
  maxLevel: number;
  listItemDrop?: IndentDropPreview["listItemDrop"];
}

type IndentDropCursorMeta =
  | { type: "preview"; preview: IndentDropPreview }
  | { type: "clear" };

export const INDENT_DROP_CURSOR_KEY = new PluginKey<IndentDropPreview | null>(
  "haloIndentDropCursor"
);

export const ExtensionDropcursor = Dropcursor.extend<DropcursorOptions>({
  addProseMirrorPlugins() {
    return [
      createIndentDropCursorPlugin(
        () => getHaloEditorIndentationSettings(this.editor),
        this.options
      ),
    ];
  },
}).configure({
  width: 2,
  class: "dropcursor",
  color: "skyblue",
});

export function createIndentDropCursorPlugin(
  getIndentationSettings: () => HaloEditorIndentationSettings,
  options: Partial<DropcursorOptions> = {}
) {
  return new Plugin<IndentDropPreview | null>({
    key: INDENT_DROP_CURSOR_KEY,
    state: {
      init: () => null,
      apply(transaction, preview) {
        const meta = transaction.getMeta(INDENT_DROP_CURSOR_KEY) as
          | IndentDropCursorMeta
          | undefined;
        if (meta?.type === "clear") {
          return null;
        }
        if (meta?.type === "preview") {
          return meta.preview;
        }
        if (!preview || !transaction.docChanged) {
          return preview;
        }
        return {
          ...preview,
          pos: transaction.mapping.map(preview.pos),
          listItemDrop: preview.listItemDrop
            ? {
                ...preview.listItemDrop,
                insertPos: transaction.mapping.map(
                  preview.listItemDrop.insertPos
                ),
              }
            : undefined,
          sourceRange: preview.sourceRange
            ? {
                from: transaction.mapping.map(preview.sourceRange.from),
                to: transaction.mapping.map(preview.sourceRange.to),
              }
            : undefined,
        };
      },
    },
    appendTransaction(transactions, _oldState, newState) {
      if (
        transactions.some((transaction) =>
          transaction.getMeta(DROP_INDENT_TRANSACTION_META)
        ) ||
        !transactions.some(
          (transaction) => transaction.getMeta("uiEvent") === "drop"
        )
      ) {
        return null;
      }

      const preview = INDENT_DROP_CURSOR_KEY.getState(newState);
      if (!preview) {
        return null;
      }

      const tr = newState.tr.setMeta(INDENT_DROP_CURSOR_KEY, {
        type: "clear",
      } satisfies IndentDropCursorMeta);
      const droppedPos = findDroppedBlockPos(newState);
      const node = droppedPos === null ? null : newState.doc.nodeAt(droppedPos);
      if (node && isNodeIndentable(node.type)) {
        const indent = indentLevelToValue(preview.level, preview);
        if (Number(node.attrs.indent) !== indent || node.attrs.lineIndent) {
          tr.setNodeMarkup(
            droppedPos,
            node.type,
            { ...node.attrs, indent, lineIndent: false },
            node.marks
          );
        }
      }

      return tr.setMeta(DROP_INDENT_TRANSACTION_META, true);
    },
    props: {
      handleDrop(view, event, slice, moved) {
        // Dragover events can be throttled while crossing deeply nested rows.
        // Resolve the final pointer position again at drop time so an older
        // preview cannot send the block to a shallower list item.
        const preview =
          computeIndentDropPreview(view, event, getIndentationSettings()) ??
          INDENT_DROP_CURSOR_KEY.getState(view.state);
        if (!preview?.normalizedListDrop) {
          return false;
        }
        if (preview.listItemDrop) {
          return dropBlockAfterListItem(view, event, slice, moved, preview);
        }
        return dropBlockAtNormalizedListBoundary(
          view,
          event,
          slice,
          moved,
          preview
        );
      },
      decorations(state) {
        const preview = INDENT_DROP_CURSOR_KEY.getState(state);
        if (!preview) {
          return null;
        }
        return DecorationSet.create(state.doc, [
          Decoration.widget(
            preview.pos,
            () => createDropCursorElement(preview, options),
            {
              key: `halo-indent-dropcursor-${preview.level}-${
                preview.listItemDrop?.insertPos ?? "root"
              }`,
              side: -1,
            }
          ),
        ]);
      },
      handleDOMEvents: {
        dragover(view, event) {
          const preview = computeIndentDropPreview(
            view,
            event,
            getIndentationSettings()
          );
          setPreview(view, preview);
          return false;
        },
        dragend(view) {
          setPreview(view, null);
          return false;
        },
        drop(view) {
          // ProseMirror's default drop handler dispatches synchronously after
          // plugin handlers return. Clear on the next task so appendTransaction
          // can still apply the chosen level to the dropped node.
          window.setTimeout(() => setPreview(view, null), 0);
          return false;
        },
        dragleave(view, event) {
          if (
            !event.relatedTarget ||
            !view.dom.contains(event.relatedTarget as Node)
          ) {
            setPreview(view, null);
          }
          return false;
        },
      },
    },
  });
}

export function computeIndentDropPreview(
  view: EditorView,
  event: Pick<DragEvent, "clientX" | "clientY">,
  indentation: number | HaloEditorIndentationSettings
): IndentDropPreview | null {
  const settings =
    typeof indentation === "number"
      ? resolveHaloEditorIndentationSettings({
          indentRange: indentation,
          minIndentLevel: 0,
          maxIndentLevel: null,
          defaultIndentLevel: 0,
        })
      : indentation;
  const { indentRange, minIndentLevel, maxIndentLevel } = settings;
  const configuredMaxLevel = Math.max(
    0,
    Math.ceil(maxIndentLevel / indentRange)
  );
  const draggedSlice = view.dragging?.slice;
  const draggedNode = draggedSlice?.content.firstChild;
  const draggedList = draggedNode?.type.isInGroup("list") ?? false;
  if (draggedNode && !isNodeIndentable(draggedNode.type) && !draggedList) {
    return null;
  }

  const editorRect = view.dom.getBoundingClientRect();
  const pointerCoords = view.posAtCoords({
    left: event.clientX,
    top: event.clientY,
  });
  const coords =
    view.posAtCoords({
      // Probe near the right content edge so moving horizontally into an
      // indented node's left margin does not accidentally select the previous
      // block. The original x coordinate still identifies list rows and the
      // chosen indentation level.
      left: Math.max(editorRect.left + 1, editorRect.right - 4),
      top: event.clientY,
    }) ?? pointerCoords;
  if (!coords) {
    return null;
  }

  const target = draggedSlice
    ? dropPoint(view.state.doc, coords.pos, draggedSlice)
    : null;
  const rawPos = target ?? coords.pos;
  const normalizedListDrop = normalizeListDropPosition(
    view,
    pointerCoords?.pos ?? coords.pos,
    rawPos,
    event.clientY
  );
  const pos = normalizedListDrop?.pos ?? rawPos;
  const draggedSelection = getDraggedSelection(view);
  if (normalizedListDrop?.listItemDrop) {
    if (
      draggedSelection &&
      normalizedListDrop.listItemDrop.insertPos >= draggedSelection.from &&
      normalizedListDrop.listItemDrop.insertPos <= draggedSelection.to
    ) {
      // A list cannot be dropped into one of its own rows. Besides producing
      // an invalid tree, deleting the source would invalidate the row anchor.
      return null;
    }
    return {
      pos,
      // A row inside a nested list already owns its horizontal level through
      // list structure. Reflect that exact level instead of pretending the
      // pointer can choose a different one without changing list semantics.
      level: clampLevel(
        draggedNode ? normalizedListDrop.maxLevel : 0,
        configuredMaxLevel
      ),
      indentRange,
      minIndentLevel,
      maxIndentLevel,
      normalizedListDrop: true,
      listItemDrop: normalizedListDrop.listItemDrop,
      sourceRange: draggedSelection
        ? { from: draggedSelection.from, to: draggedSelection.to }
        : undefined,
    };
  }

  const $pos = view.state.doc.resolve(pos);
  if ($pos.depth !== 0) {
    return null;
  }

  const referenceNode = $pos.nodeBefore ?? $pos.nodeAfter;
  if (!referenceNode) {
    return {
      pos,
      level: 0,
      indentRange,
      minIndentLevel,
      maxIndentLevel,
    };
  }

  const referencePos = $pos.nodeBefore ? pos - $pos.nodeBefore.nodeSize : pos;
  const referenceDOM = view.nodeDOM(referencePos);
  const referenceRect =
    referenceDOM instanceof HTMLElement
      ? referenceDOM.getBoundingClientRect()
      : view.dom.getBoundingClientRect();
  const referenceIndent = clampIndentLevel(
    normalizedListDrop
      ? normalizedListDrop.maxLevel * indentRange
      : Number(referenceNode.attrs.indent) || 0,
    settings
  );
  const maxLevel =
    draggedNode && !draggedList
      ? (normalizedListDrop?.maxLevel ??
        Math.max(0, Math.round(referenceIndent / indentRange)))
      : 0;
  const baseLeft = normalizedListDrop
    ? referenceRect.left
    : referenceRect.left - referenceIndent;
  return {
    pos,
    level: clampLevel(
      Math.round((event.clientX - baseLeft) / indentRange),
      Math.min(maxLevel, configuredMaxLevel)
    ),
    indentRange,
    minIndentLevel,
    maxIndentLevel,
    normalizedListDrop: Boolean(normalizedListDrop),
    listItemDrop: normalizedListDrop?.listItemDrop,
    sourceRange: draggedSelection
      ? { from: draggedSelection.from, to: draggedSelection.to }
      : undefined,
  };
}

function normalizeListDropPosition(
  view: EditorView,
  coordsPos: number,
  pos: number,
  clientY: number
): NormalizedListDrop | null {
  const itemTarget = resolveListItemDrop(view, coordsPos);
  if (itemTarget) {
    return itemTarget;
  }

  const $pos = view.state.doc.resolve(pos);
  if ($pos.depth === 0) {
    return null;
  }

  const topLevelNode = $pos.node(1);
  if (!topLevelNode.type.isInGroup("list")) {
    return null;
  }

  const topLevelPos = $pos.before(1);
  const topLevelDOM = view.nodeDOM(topLevelPos);
  const rect =
    topLevelDOM instanceof HTMLElement
      ? topLevelDOM.getBoundingClientRect()
      : null;
  const after = !rect || clientY >= rect.top + rect.height / 2;
  const listDepth = findAncestorListItems($pos).length;

  return {
    pos: topLevelPos + (after ? topLevelNode.nodeSize : 0),
    maxLevel: Math.max(1, listDepth),
  };
}

function resolveListItemDrop(
  view: EditorView,
  pos: number
): NormalizedListDrop | null {
  const $pos = view.state.doc.resolve(pos);
  const listItems = findAncestorListItems($pos);
  const activeListItem = listItems[0];
  if (
    !activeListItem ||
    activeListItem.depth < 2 ||
    !$pos.node(1).type.isInGroup("list")
  ) {
    return null;
  }

  const listPos = $pos.before(1);
  const item = activeListItem.node;
  const itemPos = activeListItem.pos;
  const leadingBlock = item.firstChild;
  if (!leadingBlock) {
    return null;
  }
  const insertPos = itemPos + 1 + leadingBlock.nodeSize;
  const listDepth = listItems.length;
  const listDOM = view.nodeDOM(listPos);
  const rowDOM = view.nodeDOM(itemPos + 1);
  const listRect =
    listDOM instanceof HTMLElement
      ? listDOM.getBoundingClientRect()
      : view.dom.getBoundingClientRect();
  const rowRect =
    rowDOM instanceof HTMLElement ? rowDOM.getBoundingClientRect() : listRect;

  return {
    // Keep the widget decoration at the root list boundary to avoid inserting
    // DOM between list children. Its fixed-positioned line is anchored to the
    // exact row, while the document insertion happens inside that list item.
    pos: listPos,
    maxLevel: Math.max(1, listDepth),
    listItemDrop: {
      insertPos,
      depth: Math.max(1, listDepth),
      anchorLeft: rowRect.left,
      anchorRight: listRect.right,
      anchorTop: rowRect.bottom,
    },
  };
}

function dropBlockAfterListItem(
  view: EditorView,
  event: DragEvent,
  slice: Slice,
  moved: boolean,
  preview: IndentDropPreview
) {
  const node = slice.content.firstChild;
  const target = preview.listItemDrop;
  if (
    !target ||
    (moved && !preview.sourceRange) ||
    slice.openStart !== 0 ||
    slice.openEnd !== 0 ||
    slice.content.childCount !== 1 ||
    !node?.isBlock
  ) {
    return false;
  }

  const tr = view.state.tr;
  if (moved && preview.sourceRange) {
    tr.delete(preview.sourceRange.from, preview.sourceRange.to);
  }
  const insertPos = tr.mapping.map(target.insertPos);
  const $insertPos = tr.doc.resolve(insertPos);
  const parent = $insertPos.parent;
  if (
    $insertPos.depth === 0 ||
    !$insertPos.node($insertPos.depth - 1).type.isInGroup("list")
  ) {
    return false;
  }

  const droppedNode = node.type.create(
    isNodeIndentable(node.type)
      ? {
          ...node.attrs,
          // The surrounding list structure supplies the visual indentation.
          // Retaining a block's old indent would apply that depth twice.
          indent: 0,
          lineIndent: false,
        }
      : node.attrs,
    node.content,
    node.marks
  );
  if (
    !parent.canReplaceWith(
      $insertPos.index(),
      $insertPos.index(),
      droppedNode.type
    )
  ) {
    return false;
  }

  event.preventDefault();
  tr.insert(insertPos, droppedNode);
  const $insertedPos = tr.doc.resolve(insertPos);
  if (
    NodeSelection.isSelectable(droppedNode) &&
    $insertedPos.nodeAfter?.sameMarkup(droppedNode)
  ) {
    tr.setSelection(new NodeSelection($insertedPos));
  }
  view.focus();
  view.dispatch(
    tr
      .setMeta("uiEvent", "drop")
      .setMeta(DROP_INDENT_TRANSACTION_META, true)
      .setMeta(INDENT_DROP_CURSOR_KEY, {
        type: "clear",
      } satisfies IndentDropCursorMeta)
  );
  return true;
}

function dropBlockAtNormalizedListBoundary(
  view: EditorView,
  event: DragEvent,
  slice: Slice,
  moved: boolean,
  preview: IndentDropPreview
) {
  const node = slice.content.firstChild;
  if (
    (moved && !preview.sourceRange) ||
    slice.openStart !== 0 ||
    slice.openEnd !== 0 ||
    slice.content.childCount !== 1 ||
    !node?.isBlock
  ) {
    return false;
  }

  event.preventDefault();
  const tr = view.state.tr;
  if (moved && preview.sourceRange) {
    tr.delete(preview.sourceRange.from, preview.sourceRange.to);
  }
  const insertPos = tr.mapping.map(preview.pos);
  tr.replaceRangeWith(insertPos, insertPos, node);
  if (!tr.docChanged) {
    return true;
  }

  const $insertPos = tr.doc.resolve(insertPos);
  if (
    NodeSelection.isSelectable(node) &&
    $insertPos.nodeAfter?.sameMarkup(node)
  ) {
    tr.setSelection(new NodeSelection($insertPos));
  }
  view.focus();
  view.dispatch(tr.setMeta("uiEvent", "drop"));
  return true;
}

function getDraggedSelection(view: EditorView) {
  const dragging = view.dragging as {
    node?: NodeSelection;
    slice: Slice;
    move: boolean;
  } | null;
  if (dragging?.node instanceof NodeSelection) {
    return dragging.node;
  }
  if (view.state.selection instanceof NodeSelection) {
    return view.state.selection;
  }
  return null;
}

function createDropCursorElement(
  preview: IndentDropPreview,
  options: Partial<DropcursorOptions>
) {
  const element = document.createElement("div");
  element.className = ["halo-indent-dropcursor", options.class]
    .filter(Boolean)
    .join(" ");
  element.contentEditable = "false";
  element.dataset.indentLevel = preview.level.toString();
  element.style.setProperty(
    "--halo-drop-indent",
    `${indentLevelToValue(preview.level, preview)}px`
  );
  element.style.height = `${options.width ?? 2}px`;
  if (preview.listItemDrop) {
    const left = preview.listItemDrop.anchorLeft;
    element.dataset.listItemTarget = "true";
    element.style.setProperty("--halo-drop-left", `${left}px`);
    element.style.setProperty(
      "--halo-drop-width",
      `${Math.max(0, preview.listItemDrop.anchorRight - left)}px`
    );
    element.style.setProperty(
      "--halo-drop-top",
      `${preview.listItemDrop.anchorTop}px`
    );
  }
  if (options.color !== false) {
    const color = options.color || "black";
    element.style.setProperty("--halo-drop-color", color);
  }
  element.setAttribute("aria-label", `Indent level ${preview.level}`);
  return element;
}

function setPreview(view: EditorView, preview: IndentDropPreview | null) {
  const current = INDENT_DROP_CURSOR_KEY.getState(view.state);
  if (
    current?.pos === preview?.pos &&
    current?.level === preview?.level &&
    current?.indentRange === preview?.indentRange &&
    current?.minIndentLevel === preview?.minIndentLevel &&
    current?.maxIndentLevel === preview?.maxIndentLevel &&
    current?.normalizedListDrop === preview?.normalizedListDrop &&
    current?.listItemDrop?.insertPos === preview?.listItemDrop?.insertPos &&
    current?.listItemDrop?.depth === preview?.listItemDrop?.depth &&
    current?.listItemDrop?.anchorLeft === preview?.listItemDrop?.anchorLeft &&
    current?.listItemDrop?.anchorRight === preview?.listItemDrop?.anchorRight &&
    current?.listItemDrop?.anchorTop === preview?.listItemDrop?.anchorTop &&
    current?.sourceRange?.from === preview?.sourceRange?.from &&
    current?.sourceRange?.to === preview?.sourceRange?.to
  ) {
    return;
  }
  if (!current && !preview) {
    return;
  }

  view.dispatch(
    view.state.tr
      .setMeta(
        INDENT_DROP_CURSOR_KEY,
        preview
          ? ({ type: "preview", preview } satisfies IndentDropCursorMeta)
          : ({ type: "clear" } satisfies IndentDropCursorMeta)
      )
      .setMeta("addToHistory", false)
  );
}

function findDroppedBlockPos(state: EditorState) {
  if (state.selection instanceof NodeSelection) {
    return state.selection.from;
  }

  const { $from } = state.selection;
  if ($from.depth === 0) {
    return $from.pos;
  }
  return $from.before(1);
}

function clampLevel(level: number, maxLevel: number) {
  return Math.min(Math.max(level, 0), maxLevel);
}
