import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolbarSubItem from "@/components/toolbar/ToolbarSubItem.vue";
import { i18n } from "@/locales";
import {
  CoreEditor,
  EditorState,
  ResolvedPos,
  TextSelection,
  isActive,
  type Dispatch,
  type Editor,
} from "@/tiptap";
import type { ExtensionOptions, ToolbarItem as TypeToolbarItem } from "@/types";
import { deleteNodeByPos } from "@/utils";
import { isListActive } from "@/utils/isListActive";
import { isEmpty } from "@/utils/isNodeEmpty";
import type { ParagraphOptions } from "@tiptap/extension-paragraph";
import TiptapParagraph from "@tiptap/extension-paragraph";
import { markRaw } from "vue";
import TablerLineHeight from "~icons/tabler/line-height";

const Paragraph = TiptapParagraph.extend<ExtensionOptions & ParagraphOptions>({
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
      getDraggable() {
        return {
          getRenderContainer({ dom }) {
            let container = dom;
            while (container && container.tagName !== "P") {
              container = container.parentElement as HTMLElement;
            }
            return {
              el: container,
              dragDomOffset: {
                y: -1,
              },
            };
          },
          allowPropagationDownward: true,
        };
      },
      getToolbarItems({ editor }: { editor: Editor }): TypeToolbarItem {
        return {
          priority: 220,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: !!editor.getAttributes(Paragraph.name)?.lineHeight,
            icon: markRaw(TablerLineHeight),
            title: i18n.global.t("editor.common.line_height"),
          },
          children: [0, 1, 1.5, 2, 2.5, 3].map((lineHeight) => {
            return {
              priority: lineHeight,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive:
                  editor.getAttributes(Paragraph.name)?.lineHeight ===
                  lineHeight,
                title: !lineHeight
                  ? i18n.global.t("editor.common.text.default")
                  : String(lineHeight),
                action: () =>
                  editor
                    .chain()
                    .focus()
                    .updateAttributes(Paragraph.name, {
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

  addKeyboardShortcuts() {
    return {
      Backspace: ({ editor }: { editor: CoreEditor }) => {
        const { state, view } = editor;
        const { selection } = state;
        if (isListActive(editor) || !isActive(state, Paragraph.name)) {
          return false;
        }

        if (!(selection instanceof TextSelection) || !selection.empty) {
          return false;
        }

        const { $from } = selection;

        if ($from.parentOffset !== 0) {
          return false;
        }

        const beforePos = $from.before($from.depth);
        if (isEmpty($from.parent)) {
          return deleteCurrentNodeAndSetSelection(
            $from,
            beforePos,
            state,
            view.dispatch
          );
        }

        if (beforePos === 0) {
          return false;
        }

        return handleDeletePreviousNode($from, beforePos, state, view.dispatch);
      },
    };
  },
});

export function deleteCurrentNodeAndSetSelection(
  $from: ResolvedPos,
  beforePos: number,
  state: EditorState,
  dispatch: Dispatch
) {
  const { tr } = state;
  if (deleteNodeByPos($from)(tr) && dispatch) {
    if (beforePos !== 0) {
      tr.setSelection(TextSelection.near(tr.doc.resolve(beforePos - 1), -1));
    }
    dispatch(tr);
    return true;
  }
  return false;
}

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

  if (
    !nodeBefore ||
    !nodeBefore.type.isBlock ||
    nodeBefore.type.isText ||
    nodeBefore.type.name === Paragraph.name
  ) {
    return false;
  }

  if (deleteNodeByPos($from.doc.resolve(beforePos - 1))(tr)) {
    dispatch(tr);
    return true;
  }
  return false;
}

export default Paragraph;
