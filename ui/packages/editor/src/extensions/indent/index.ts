import {
  DEFAULT_INDENT_RANGE,
  DEFAULT_MIN_INDENT_LEVEL,
  DROP_INDENT_TRANSACTION_META,
  getNodeIndentationMetadata,
  type HaloEditorIndentationOptions,
  INDENT_TRANSACTION_META,
  isNodeIndentable,
  resolveHaloEditorIndentationSettings,
  resolveNodeIndentationMetadata,
} from "@/editor-metadata/indentation";
import {
  combineTransactionSteps,
  Extension,
  getChangedRanges,
  mergeAttributes,
  type CommandProps,
  type Editor,
  type KeyboardShortcutCommand,
} from "@/tiptap";
import {
  Decoration,
  DecorationSet,
  GapCursor,
  isHistoryTransaction,
  NodeSelection,
  Plugin,
  PluginKey,
  TextSelection,
  Transaction,
} from "@/tiptap/pm";
import type { ExtensionOptions } from "@/types";
import { findAncestorListItems, resolveGapCursorSide } from "@/utils";
const INDENT_PLUGIN_KEY = new PluginKey("haloIndent");

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    indent: {
      indent: () => ReturnType;
      outdent: () => ReturnType;
    };
  }
}

export interface ExtensionIndentOptions
  extends ExtensionOptions, HaloEditorIndentationOptions {
  /**
   * Explicit compatibility override. `null` discovers indentable block nodes
   * from their schema metadata, including third-party extensions.
   */
  names: Array<string> | null;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  HTMLAttributes: Record<string, any>;
  firstLineIndent: boolean;
}

export const ExtensionIndent = Extension.create<ExtensionIndentOptions>({
  name: "indent",
  // Run before the gap-cursor fallback so Tab can indent an adjacent block.
  priority: 950,

  addHaloEditorMetadata() {
    const settings = resolveHaloEditorIndentationSettings(this.options);
    return {
      contributions: Object.values(this.editor.schema.nodes)
        .filter(isNodeIndentable)
        .map((type) => {
          const metadata = getNodeIndentationMetadata(type);
          return {
            targets: [{ kind: "node" as const, name: type.name }],
            metadata: {
              ai: {
                attributeGuidance: {
                  indent: {
                    description: "Left indentation in pixels.",
                    examples: [
                      settings.minIndentLevel,
                      settings.indentRange,
                      settings.maxIndentLevel,
                    ],
                    guidelines: [
                      `Use values between ${settings.minIndentLevel} and ${settings.maxIndentLevel}, normally in increments of ${settings.indentRange}.`,
                    ],
                    omitWhen: ["No block indentation is needed."],
                  },
                  ...(metadata.legacyLineIndent
                    ? {
                        lineIndent: {
                          description:
                            "Legacy first-line indentation kept for existing content. Prefer indent for new content.",
                          allowedValues: [true, false],
                          guidelines: [
                            "Do not add lineIndent to newly generated content.",
                          ],
                          omitWhen: ["Generating or editing new content."],
                        },
                      }
                    : {}),
                },
              },
            },
          };
        }),
    };
  },

  addOptions() {
    return {
      names: null,
      indentRange: DEFAULT_INDENT_RANGE,
      minIndentLevel: DEFAULT_MIN_INDENT_LEVEL,
      maxIndentLevel: null,
      defaultIndentLevel: DEFAULT_MIN_INDENT_LEVEL,
      HTMLAttributes: {},
      firstLineIndent: true,
    };
  },

  onBeforeCreate() {
    const settings = resolveHaloEditorIndentationSettings(this.options);
    for (const extension of this.editor.extensionManager.extensions) {
      if (extension.type !== "node") {
        continue;
      }
      const type = this.editor.schema.nodes[extension.name];
      if (!type) {
        continue;
      }
      type.spec.haloEditorIndentation = resolveNodeIndentationMetadata(
        extension,
        this.options.names,
        settings
      );
    }
  },

  addGlobalAttributes() {
    const settings = resolveHaloEditorIndentationSettings(this.options);
    const indentation = this.extensions
      .map((extension) => ({
        extension,
        metadata: resolveNodeIndentationMetadata(
          extension,
          this.options.names,
          settings
        ),
      }))
      .filter(({ metadata }) => metadata.enabled);
    const indentableNames = indentation.map(({ extension }) => extension.name);
    const legacyLineIndentNames = this.options.firstLineIndent
      ? indentation
          .filter(({ metadata }) => metadata.legacyLineIndent)
          .map(({ extension }) => extension.name)
      : [];
    return [
      {
        types: indentableNames,
        attributes: {
          indent: {
            default: settings.defaultIndentLevel,
            keepOnSplit: true,
            renderHTML: (attributes) =>
              mergeAttributes(
                this.options.HTMLAttributes,
                getIndentHTMLAttributes(attributes.indent)
              ),
            parseHTML: (element) => {
              const serializedIndent = Number.parseInt(
                element.getAttribute("data-indent") || "",
                10
              );
              if (Number.isFinite(serializedIndent)) {
                return serializedIndent;
              }
              return (
                Number.parseInt(element.style.marginLeft, 10) ||
                settings.defaultIndentLevel
              );
            },
          },
        },
      },
      {
        types: legacyLineIndentNames,
        attributes: {
          lineIndent: {
            default: false,
            keepOnSplit: false,
            renderHTML: (attributes) => ({
              style: attributes.lineIndent ? "text-indent: 2em" : "",
            }),
            parseHTML: (element) => element.style.textIndent === "2em",
          },
        },
      },
    ];
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: INDENT_PLUGIN_KEY,
        appendTransaction: (transactions, oldState, newState) => {
          if (
            !transactions.some((transaction) => transaction.docChanged) ||
            transactions.some(isHistoryTransaction) ||
            transactions.some((transaction) =>
              transaction.getMeta(DROP_INDENT_TRANSACTION_META)
            ) ||
            transactions.some((transaction) =>
              transaction.getMeta(INDENT_TRANSACTION_META)
            )
          ) {
            return null;
          }

          const transform = combineTransactionSteps(oldState.doc, [
            ...transactions,
          ]);
          const changedRanges = getChangedRanges(transform);
          const tr = newState.tr;
          const updatedPositions = new Set<number>();
          const indentContext = getSelectionIndentContext(oldState.selection);

          for (const { oldRange, newRange } of changedRanges) {
            const from = clamp(newRange.from, 0, newState.doc.content.size);
            const to = clamp(newRange.to, from, newState.doc.content.size);
            const expandedRange = expandToChangedTopLevelNodes(
              newState.doc,
              from,
              to
            );
            newState.doc.nodesBetween(
              expandedRange.from,
              expandedRange.to,
              (node, pos) => {
                if (!isNodeIndentable(node.type)) {
                  return true;
                }
                if (updatedPositions.has(pos)) {
                  return false;
                }

                if (isDirectChildOfListItem(newState.doc, pos)) {
                  if (Number(node.attrs.indent) > 0 || node.attrs.lineIndent) {
                    tr.setNodeMarkup(
                      pos,
                      node.type,
                      { ...node.attrs, indent: 0, lineIndent: false },
                      node.marks
                    );
                    updatedPositions.add(pos);
                  }
                  return false;
                }

                if (
                  !indentContext ||
                  indentContext.indent <= 0 ||
                  !rangesTouch(oldRange, indentContext) ||
                  Number(node.attrs.indent) > 0
                ) {
                  return false;
                }
                tr.setNodeMarkup(
                  pos,
                  node.type,
                  { ...node.attrs, indent: indentContext.indent },
                  node.marks
                );
                updatedPositions.add(pos);
                return false;
              }
            );
          }

          return tr.docChanged ? tr : null;
        },
        props: {
          decorations(state) {
            const decorations: Decoration[] = [];
            state.doc.descendants((node, pos) => {
              if (!isNodeIndentable(node.type)) {
                return true;
              }
              const indent = Number(node.attrs.indent) || 0;
              if (indent <= 0) {
                return true;
              }
              decorations.push(
                Decoration.node(
                  pos,
                  pos + node.nodeSize,
                  getIndentHTMLAttributes(indent)
                )
              );
              return true;
            });
            return DecorationSet.create(state.doc, decorations);
          },
        },
      }),
    ];
  },

  addCommands(this) {
    return {
      indent:
        () =>
        ({ tr, state, dispatch }: CommandProps) => {
          const { selection } = state;
          tr = tr.setSelection(selection);
          tr = updateIndentLevel(
            tr,
            resolveHaloEditorIndentationSettings(this.options),
            "indent"
          );
          if (tr.docChanged) {
            tr.setMeta(INDENT_TRANSACTION_META, true);
          }
          if (tr.docChanged && dispatch) {
            dispatch(tr);
          }
          return tr.docChanged;
        },
      outdent:
        () =>
        ({ tr, state, dispatch }: CommandProps) => {
          const { selection } = state;
          tr = tr.setSelection(selection);
          tr = updateIndentLevel(
            tr,
            resolveHaloEditorIndentationSettings(this.options),
            "outdent"
          );
          if (tr.docChanged) {
            tr.setMeta(INDENT_TRANSACTION_META, true);
          }
          if (tr.docChanged && dispatch) {
            dispatch(tr);
          }
          return tr.docChanged;
        },
    };
  },

  addKeyboardShortcuts() {
    return {
      Tab: getIndent(),
      "Shift-Tab": getOutdent(false, true),
      "Mod-]": getIndent(),
      "Mod-[": getOutdent(false, true),
      Backspace: ({ editor }) => {
        const { selection } = editor.state;
        if (
          selection instanceof GapCursor ||
          selection instanceof NodeSelection ||
          (selection instanceof TextSelection &&
            selection.empty &&
            selection.$from.parentOffset === 0)
        ) {
          return getOutdent(false)({ editor });
        }
        return false;
      },
      Delete: ({ editor }) => {
        if (editor.state.selection instanceof GapCursor) {
          return getOutdent(false)({ editor });
        }
        return false;
      },
    };
  },
});

export const clamp = (val: number, min: number, max: number): number => {
  if (val < min) {
    return min;
  }
  if (val > max) {
    return max;
  }
  return val;
};

function setNodeIndentMarkup(
  tr: Transaction,
  pos: number,
  dir: number,
  settings: HaloEditorIndentationOptions
): Transaction {
  if (!tr.doc) {
    return tr;
  }
  const node = tr.doc.nodeAt(pos);
  if (!node) {
    return tr;
  }
  const options = resolveHaloEditorIndentationSettings(settings);
  const delta = options.indentRange * dir;
  const min = options.minIndentLevel;
  const max = options.maxIndentLevel;
  const currentIndent =
    Number(node.attrs.indent) ||
    (node.attrs.lineIndent ? options.indentRange : 0);
  const indent = clamp(currentIndent + delta, min, max);
  if (indent === node.attrs.indent && !node.attrs.lineIndent) {
    return tr;
  }
  const nodeAttrs = { ...node.attrs, indent, lineIndent: false };
  return tr.setNodeMarkup(pos, node.type, nodeAttrs, node.marks);
}

type IndentType = "indent" | "outdent";
const updateIndentLevel = (
  tr: Transaction,
  options: HaloEditorIndentationOptions,
  type: IndentType
): Transaction => {
  const { doc, selection } = tr;
  if (!doc || !selection) return tr;

  const dir = type === "indent" ? 1 : -1;
  if (selection instanceof GapCursor) {
    const target = findGapCursorIndentTarget(selection, type);
    if (!target || !isNodeIndentable(target.node.type)) {
      return tr;
    }
    return setNodeIndentMarkup(tr, target.pos, dir, options);
  }

  if (selection instanceof NodeSelection) {
    if (!isNodeIndentable(selection.node.type)) {
      return tr;
    }
    return setNodeIndentMarkup(tr, selection.from, dir, options);
  }

  if (!(selection instanceof TextSelection)) {
    return tr;
  }
  const { from, to } = selection;
  doc.nodesBetween(from, to, (node, pos) => {
    if (isNodeIndentable(node.type)) {
      const nodeEnd = pos + node.nodeSize;
      const nodeIsFullySelected = from <= pos && to >= nodeEnd;
      if (node.isTextblock || nodeIsFullySelected) {
        tr = setNodeIndentMarkup(tr, pos, dir, options);
        return false;
      }

      // A text cursor inside a structural component belongs to its inner text
      // block. Continue into the component instead of indenting its wrapper.
      return true;
    }
    return !node.type.isInGroup("list");
  });

  return tr;
};

function findGapCursorIndentTarget(selection: GapCursor, type?: IndentType) {
  const { $from } = selection;
  const preferredSide = (selection as GapCursor & { side?: "before" | "after" })
    .side;
  const side = resolveGapCursorSide($from, preferredSide);

  // A leading gap represents the insertion/indentation point for the block.
  // The trailing gap is a navigation stop and must not indent the block that
  // was already passed. Outdent remains available there for Delete/Shift-Tab.
  if (type === "indent" && side === "after") {
    return null;
  }

  if (side === "before" && $from.nodeAfter) {
    return { node: $from.nodeAfter, pos: $from.pos };
  }
  if (side === "after" && $from.nodeBefore) {
    return {
      node: $from.nodeBefore,
      pos: $from.pos - $from.nodeBefore.nodeSize,
    };
  }
  return null;
}

function getSelectionIndentContext(selection: Transaction["selection"]) {
  if (selection instanceof GapCursor) {
    const target = findGapCursorIndentTarget(selection);
    if (!target) {
      return null;
    }
    return {
      indent: Number(target.node.attrs.indent) || 0,
      from: target.pos,
      to: target.pos + target.node.nodeSize,
    };
  }
  if (selection instanceof NodeSelection) {
    return {
      indent: Number(selection.node.attrs.indent) || 0,
      from: selection.from,
      to: selection.to,
    };
  }
  const { $from } = selection;
  if ($from.depth === 0) {
    return null;
  }
  const from = $from.before($from.depth);
  return {
    indent: Number($from.parent.attrs.indent) || 0,
    from,
    to: from + $from.parent.nodeSize,
  };
}

function rangesTouch(
  range: { from: number; to: number },
  context: { from: number; to: number }
) {
  return range.from <= context.to && range.to >= context.from;
}

function expandToChangedTopLevelNodes(
  doc: Transaction["doc"],
  from: number,
  to: number
) {
  let expandedFrom = from;
  let expandedTo = to;
  let found = false;

  doc.forEach((node, pos) => {
    const end = pos + node.nodeSize;
    if (end < from || pos > to) {
      return;
    }
    expandedFrom = found ? Math.min(expandedFrom, pos) : pos;
    expandedTo = found ? Math.max(expandedTo, end) : end;
    found = true;
  });

  return { from: expandedFrom, to: expandedTo };
}

function isDirectChildOfListItem(doc: Transaction["doc"], pos: number) {
  const $pos = doc.resolve(pos);
  return $pos.depth > 0 && $pos.node($pos.depth - 1).type.isInGroup("list");
}

function getIndentHTMLAttributes(indent: unknown) {
  const value = Number(indent) || 0;
  if (value <= 0) {
    return {};
  }
  return {
    "data-indent": value.toString(),
    style: `margin-left: ${value}px!important;max-width: calc(100% - ${value}px)!important;`,
  };
}

export const getIndent: () => KeyboardShortcutCommand =
  () =>
  ({ editor }) => {
    const { selection } = editor.state;
    if (hasPassthroughIndentationAncestor(selection)) {
      return false;
    }
    const listItemName = getActiveListItemName(editor);
    if (listItemName) {
      editor.chain().focus().sinkListItem(listItemName).run();
      return true;
    }
    return (
      editor.chain().focus().indent().run() || hasIndentableSelection(editor)
    );
  };
export const getOutdent: (
  outdentOnlyAtHead: boolean,
  consumeAtBoundary?: boolean
) => KeyboardShortcutCommand =
  (outdentOnlyAtHead, consumeAtBoundary = false) =>
  ({ editor }) => {
    if (outdentOnlyAtHead && editor.state.selection.$head.parentOffset > 0) {
      return false;
    }

    const { selection } = editor.state;
    if (hasPassthroughIndentationAncestor(selection)) {
      return false;
    }

    const listItemName = getActiveListItemName(editor);
    if (listItemName) {
      return (
        editor.chain().focus().liftListItem(listItemName).run() ||
        consumeAtBoundary
      );
    }
    return (
      editor.chain().focus().outdent().run() ||
      (consumeAtBoundary && hasIndentableSelection(editor))
    );
  };

function hasIndentableSelection(editor: Editor) {
  const { selection, doc } = editor.state;

  if (selection instanceof GapCursor) {
    const target = findGapCursorIndentTarget(selection);
    return Boolean(target && isNodeIndentable(target.node.type));
  }
  if (selection instanceof NodeSelection) {
    return isNodeIndentable(selection.node.type);
  }

  let found = false;
  doc.nodesBetween(selection.from, selection.to, (node) => {
    if (isNodeIndentable(node.type)) {
      found = true;
      return false;
    }
    return !found;
  });
  return found;
}

function hasPassthroughIndentationAncestor(
  selection: Transaction["selection"]
) {
  if (!(selection instanceof TextSelection)) {
    return false;
  }
  const { $from } = selection;
  for (let depth = $from.depth; depth > 0; depth--) {
    if (
      getNodeIndentationMetadata($from.node(depth).type).keyboard ===
      "passthrough"
    ) {
      return true;
    }
  }
  return false;
}

function getActiveListItemName(editor: Editor) {
  return (
    findAncestorListItems(editor.state.selection.$from)[0]?.node.type.name ??
    null
  );
}
