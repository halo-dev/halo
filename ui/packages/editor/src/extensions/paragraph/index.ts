import TiptapParagraph, {
  type ParagraphOptions,
} from "@tiptap/extension-paragraph";
import { markRaw } from "vue";
import MingcuteLineHeightLine from "~icons/mingcute/line-height-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolbarSubItem from "@/components/toolbar/ToolbarSubItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import {
  Editor,
  EditorState,
  ResolvedPos,
  TextSelection,
  type Dispatch,
} from "@/tiptap";
import type { ExtensionOptions, ToolbarItemType } from "@/types";
import { deleteNodeByPos, isGapCursorTargetNode } from "@/utils";

export type ExtensionParagraphOptions = ExtensionOptions &
  Partial<ParagraphOptions>;

const LINE_HEIGHTS = [1, 1.5, 2, 2.5, 3] as const;

function changeLineHeight(editor: Editor, direction: -1 | 1): boolean {
  const currentValue = Number.parseFloat(
    editor.getAttributes("paragraph").lineHeight ?? "1.5"
  );
  const target =
    direction > 0
      ? LINE_HEIGHTS.find((lineHeight) => lineHeight > currentValue)
      : [...LINE_HEIGHTS]
          .reverse()
          .find((lineHeight) => lineHeight < currentValue);

  if (!target) {
    return false;
  }
  return editor
    .chain()
    .focus()
    .updateAttributes("paragraph", { lineHeight: target })
    .run();
}

export const ExtensionParagraph =
  TiptapParagraph.extend<ExtensionParagraphOptions>({
    addKeyboardShortcuts() {
      return defineHaloKeyboardShortcuts(this, [
        {
          id: "editor.structure.paragraph",
          keys: ["Mod-Alt-0"],
          label: () => i18n.global.t("editor.shortcuts.commands.paragraph"),
          category: "structure",
          priority: 10,
        },
        {
          id: "editor.format.increaseLineHeight",
          keys: ["Mod-Alt-]"],
          label: () =>
            i18n.global.t("editor.shortcuts.commands.increase_line_height"),
          category: "formatting",
          priority: 115,
          command: () => changeLineHeight(this.editor, 1),
        },
        {
          id: "editor.format.decreaseLineHeight",
          keys: ["Mod-Alt-["],
          label: () =>
            i18n.global.t("editor.shortcuts.commands.decrease_line_height"),
          category: "formatting",
          priority: 116,
          command: () => changeLineHeight(this.editor, -1),
        },
      ]);
    },

    haloEditorIndentation: {
      legacyLineIndent: true,
    },

    addHaloEditorMetadata() {
      return {
        ai: {
          description: "A standard paragraph of body text.",
          exposure: "recommended",
          useWhen: ["Writing ordinary prose or separating ideas into blocks."],
          attributeGuidance: {
            lineHeight: {
              description: "CSS line-height applied to this paragraph.",
              examples: [1.5, 2, "1.6"],
              omitWhen: ["The default line height is sufficient."],
            },
          },
          generation: {
            mode: "direct-html",
          },
          examples: ["<p>A concise paragraph of body text.</p>"],
        },
      };
    },

    addAttributes() {
      return {
        lineHeight: {
          default: null,
          parseHTML: (element) => {
            return element.style.lineHeight;
          },
          renderHTML: (attributes) => {
            const lineHeight = attributes.lineHeight;
            if (!lineHeight) {
              return {};
            }
            return {
              style: `line-height: ${lineHeight}`,
            };
          },
        },
      };
    },
    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }): ToolbarItemType {
          return {
            priority: 220,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: !!editor.getAttributes(ExtensionParagraph.name)
                ?.lineHeight,
              icon: markRaw(MingcuteLineHeightLine),
              title: i18n.global.t("editor.common.line_height"),
              shortcutIds: [
                "editor.format.increaseLineHeight",
                "editor.format.decreaseLineHeight",
              ],
            },
            children: [0, ...LINE_HEIGHTS].map((lineHeight) => {
              return {
                priority: lineHeight,
                component: markRaw(ToolbarSubItem),
                props: {
                  editor,
                  isActive:
                    editor.getAttributes(ExtensionParagraph.name)
                      ?.lineHeight === lineHeight,
                  title: !lineHeight
                    ? i18n.global.t("editor.common.text.default")
                    : String(lineHeight),
                  action: () =>
                    editor
                      .chain()
                      .focus()
                      .updateAttributes(ExtensionParagraph.name, {
                        lineHeight,
                      })
                      .run(),
                },
              };
            }),
          };
        },
      };
    },
  });

/**
 * @deprecated GapCursor now owns staged deletion around structural blocks.
 */
export function deleteCurrentNodeAndSetSelection(
  $from: ResolvedPos,
  beforePos: number,
  state: EditorState,
  dispatch: Dispatch
) {
  const { tr } = state;
  const deleted = deleteNodeByPos($from)(tr);
  if (!deleted) {
    return false;
  }
  if (!dispatch) {
    return false;
  }
  if (beforePos !== 0) {
    tr.setSelection(TextSelection.near(tr.doc.resolve(beforePos - 1), -1));
  }
  dispatch(tr);
  return true;
}

/**
 * @deprecated GapCursor now owns staged deletion around structural blocks.
 */
export function handleDeletePreviousNode(
  $from: ResolvedPos,
  beforePos: number,
  state: EditorState,
  dispatch: Dispatch
) {
  const { tr } = state;
  if (!dispatch) {
    return false;
  }

  const $beforePos = $from.doc.resolve(beforePos);
  const nodeBefore = $beforePos.nodeBefore;
  if (!nodeBefore) {
    return false;
  }
  if (!nodeBefore.type.isBlock) {
    return false;
  }
  if (nodeBefore.type.isText) {
    return false;
  }
  if (nodeBefore.type.name === ExtensionParagraph.name) {
    return false;
  }
  if (!isGapCursorTargetNode(nodeBefore)) {
    return false;
  }

  const deleted = deleteNodeByPos($from.doc.resolve(beforePos - 1))(tr);
  if (!deleted) {
    return false;
  }
  dispatch(tr);
  return true;
}
