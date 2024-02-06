import {
  Editor,
  type Range,
  type CommandProps,
  isActive,
  findParentNode,
  VueNodeViewRenderer,
} from "@/tiptap/vue-3";
import { EditorState, TextSelection, type Transaction } from "@/tiptap/pm";
import CodeBlockLowlight from "@tiptap/extension-code-block-lowlight";
import type { CodeBlockLowlightOptions } from "@tiptap/extension-code-block-lowlight";
import CodeBlockViewRenderer from "./CodeBlockViewRenderer.vue";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiCodeBracesBox from "~icons/mdi/code-braces-box";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import { deleteNode } from "@/utils";

export interface CustomCodeBlockLowlightOptions
  extends CodeBlockLowlightOptions {
  lowlight: any;
  defaultLanguage: string | null | undefined;
}

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    codeIndent: {
      codeIndent: () => ReturnType;
      codeOutdent: () => ReturnType;
    };
  }
}

type IndentType = "indent" | "outdent";
const updateIndent = (tr: Transaction, type: IndentType): Transaction => {
  const { doc, selection } = tr;
  if (!doc || !selection) return tr;
  if (!(selection instanceof TextSelection)) {
    return tr;
  }
  const { from, to } = selection;
  doc.nodesBetween(from, to, (node, pos) => {
    if (from - to == 0 && type === "indent") {
      tr.insertText("\t", from, to);
      return false;
    }

    const precedeContent = doc.textBetween(pos + 1, from, "\n");
    const precedeLineBreakPos = precedeContent.lastIndexOf("\n");
    const startBetWeenIndex =
      precedeLineBreakPos === -1 ? pos + 1 : pos + precedeLineBreakPos + 1;
    const text = doc.textBetween(startBetWeenIndex, to, "\n");
    if (type === "indent") {
      let replacedStr = text.replace(/\n/g, "\n\t");
      if (startBetWeenIndex === pos + 1) {
        replacedStr = "\t" + replacedStr;
      }
      tr.insertText(replacedStr, startBetWeenIndex, to);
    } else {
      let replacedStr = text.replace(/\n\t/g, "\n");
      if (startBetWeenIndex === pos + 1) {
        const firstNewLineIndex = replacedStr.indexOf("\t");
        if (firstNewLineIndex == 0) {
          replacedStr = replacedStr.replace("\t", "");
        }
      }
      tr.insertText(replacedStr, startBetWeenIndex, to);
    }
    return false;
  });

  return tr;
};

const getRenderContainer = (node: HTMLElement) => {
  let container = node;
  // 文本节点
  if (container.nodeName === "#text") {
    container = node.parentElement as HTMLElement;
  }
  while (
    container &&
    container.classList &&
    !container.classList.contains("code-node")
  ) {
    container = container.parentElement as HTMLElement;
  }
  return container;
};

export default CodeBlockLowlight.extend<
  CustomCodeBlockLowlightOptions & CodeBlockLowlightOptions
>({
  addCommands() {
    return {
      ...this.parent?.(),
      codeIndent:
        () =>
        ({ tr, state, dispatch }: CommandProps) => {
          const { selection } = state;
          tr = tr.setSelection(selection);
          tr = updateIndent(tr, "indent");
          if (tr.docChanged && dispatch) {
            dispatch(tr);
            return true;
          }
          return false;
        },
      codeOutdent:
        () =>
        ({ tr, state, dispatch }: CommandProps) => {
          const { selection } = state;
          tr = tr.setSelection(selection);
          tr = updateIndent(tr, "outdent");
          if (tr.docChanged && dispatch) {
            dispatch(tr);
            return true;
          }
          return false;
        },
    };
  },
  addKeyboardShortcuts() {
    return {
      Tab: () => {
        if (this.editor.isActive("codeBlock")) {
          return this.editor.chain().focus().codeIndent().run();
        }
        return false;
      },
      "Shift-Tab": () => {
        if (this.editor.isActive("codeBlock")) {
          return this.editor.chain().focus().codeOutdent().run();
        }
        return false;
      },
      "Mod-a": () => {
        if (this.editor.isActive("codeBlock")) {
          const { tr, selection } = this.editor.state;
          const codeBlack = findParentNode(
            (node) => node.type.name === CodeBlockLowlight.name
          )(selection);
          if (!codeBlack) {
            return false;
          }
          const head = codeBlack.start;
          const anchor = codeBlack.start + codeBlack.node.nodeSize - 1;
          const $head = tr.doc.resolve(head);
          const $anchor = tr.doc.resolve(anchor);
          this.editor.view.dispatch(
            tr.setSelection(new TextSelection($head, $anchor))
          );
          return true;
        }
        return false;
      },
    };
  },
  addNodeView() {
    return VueNodeViewRenderer(CodeBlockViewRenderer);
  },
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 160,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("codeBlock"),
            icon: markRaw(MdiCodeBracesBox),
            title: i18n.global.t("editor.common.codeblock"),
            action: () => editor.chain().focus().toggleCodeBlock().run(),
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 80,
          icon: markRaw(MdiCodeBracesBox),
          title: "editor.common.codeblock",
          keywords: ["codeblock", "daimakuai"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor.chain().focus().deleteRange(range).setCodeBlock().run();
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 50,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(MdiCodeBracesBox),
              title: i18n.global.t("editor.common.codeblock"),
              action: () => {
                editor.chain().focus().setCodeBlock().run();
              },
            },
          },
        ];
      },
      getBubbleMenu() {
        return {
          pluginKey: "codeBlockBubbleMenu",
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, CodeBlockLowlight.name);
          },
          getRenderContainer: (node: HTMLElement) => {
            return getRenderContainer(node);
          },
          items: [
            {
              priority: 10,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }: { editor: Editor }) =>
                  deleteNode(CodeBlockLowlight.name, editor),
              },
            },
          ],
        };
      },
      getDraggable() {
        return {
          getRenderContainer({ dom }: { dom: HTMLElement }) {
            return {
              el: getRenderContainer(dom),
            };
          },
        };
      },
    };
  },
});
