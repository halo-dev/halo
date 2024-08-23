import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  EditorState,
  Plugin,
  PluginKey,
  TextSelection,
  type Transaction,
} from "@/tiptap/pm";
import {
  Editor,
  VueNodeViewRenderer,
  findParentNode,
  isActive,
  isNodeActive,
  type CommandProps,
  type Range,
} from "@/tiptap/vue-3";
import { deleteNode } from "@/utils";
import { markRaw } from "vue";
import MdiCodeBracesBox from "~icons/mdi/code-braces-box";
import CodeBlockViewRenderer from "./CodeBlockViewRenderer.vue";
import TiptapCodeBlock from "@tiptap/extension-code-block";

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
      tr.insertText("  ", from, to);
      return false;
    }

    const precedeContent = doc.textBetween(pos + 1, from, "\n");
    const precedeLineBreakPos = precedeContent.lastIndexOf("\n");
    const startBetWeenIndex =
      precedeLineBreakPos === -1 ? pos + 1 : pos + precedeLineBreakPos + 1;
    const text = doc.textBetween(startBetWeenIndex, to, "\n");
    if (type === "indent") {
      let replacedStr = text.replace(/\n/g, "\n  ");
      if (startBetWeenIndex === pos + 1) {
        replacedStr = "  " + replacedStr;
      }
      tr.insertText(replacedStr, startBetWeenIndex, to);
    } else {
      let replacedStr = text.replace(/\n {2}/g, "\n");
      if (startBetWeenIndex === pos + 1) {
        const firstNewLineIndex = replacedStr.indexOf("  ");
        if (firstNewLineIndex == 0) {
          replacedStr = replacedStr.replace("  ", "");
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

export interface Option {
  label: string;
  value: string;
}

export interface CodeBlockOptions {
  /**
   * Define whether the node should be exited on triple enter.
   * @default true
   */
  exitOnTripleEnter: boolean;
  /**
   * Define whether the node should be exited on arrow down if there is no node after it.
   * @default true
   */
  exitOnArrowDown: boolean;
  /**
   * Custom HTML attributes that should be added to the rendered HTML tag.
   * @default {}
   * @example { class: 'foo' }
   */
  HTMLAttributes: Record<string, any>;

  /**
   * The default language for code block
   * @default null
   */
  defaultLanguage: string | null | undefined;

  /**
   * The default theme for code block
   * @default null
   */
  defaultTheme: string | null | undefined;
}

export interface ExtensionCodeBlockOptions extends CodeBlockOptions {
  /**
   * Used for language list
   *
   * @default []
   */
  languages:
    | Array<Option>
    | ((state: EditorState) => Array<{
        label: string;
        value: string;
      }>);

  /**
   * Used for theme list
   *
   * @default []
   */
  themes?:
    | Array<{
        label: string;
        value: string;
      }>
    | ((state: EditorState) => Array<{
        label: string;
        value: string;
      }>);
}

export default TiptapCodeBlock.extend<ExtensionCodeBlockOptions>({
  allowGapCursor: true,
  // It needs to have a higher priority than range-selection,
  // otherwise the Mod-a shortcut key will be overridden.
  priority: 110,

  fakeSelection: true,

  addAttributes() {
    return {
      ...this.parent?.(),
      collapsed: {
        default: false,
        parseHTML: (element) => !!element.getAttribute("collapsed"),
        renderHTML: (attributes) => {
          if (attributes.collapsed) {
            return {
              collapsed: attributes.collapsed,
            };
          }
          return {};
        },
      },
      theme: {
        default: this.options.defaultTheme,
        parseHTML: (element) => element.getAttribute("theme") || null,
        renderHTML: (attributes) => {
          if (attributes.theme) {
            return {
              theme: attributes.theme,
            };
          }
          return {};
        },
      },
    };
  },

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
      Backspace: ({ editor }) => {
        if (!isNodeActive(editor.state, this.name)) {
          return false;
        }

        const { selection } = editor.state;
        // Clear the selected content and adapt to the all-select shortcut key operation.
        if (!selection.empty) {
          editor
            .chain()
            .focus()
            .deleteSelection()
            .setTextSelection(selection.$from.pos)
            .run();
          return true;
        }

        const { $anchor } = selection;
        const isAtStart = $anchor.parentOffset === 0;
        // If the cursor is at the beginning of the code block or the code block is empty, it is not deleted.
        if (isAtStart || !$anchor.parent.textContent.length) {
          return true;
        }

        return false;
      },
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
            (node) => node.type.name === TiptapCodeBlock.name
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
      languages: [],
      themes: [],
      defaultLanguage: null,
      defaultTheme: null,
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 160,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("codeBlock"),
            icon: markRaw(MdiCodeBracesBox),
            title: i18n.global.t("editor.common.codeblock.title"),
            action: () => editor.chain().focus().toggleCodeBlock().run(),
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 80,
          icon: markRaw(MdiCodeBracesBox),
          title: "editor.common.codeblock.title",
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
              title: i18n.global.t("editor.common.codeblock.title"),
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
            return isActive(state, TiptapCodeBlock.name);
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
                  deleteNode(TiptapCodeBlock.name, editor),
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

  addProseMirrorPlugins() {
    return [
      // Solve the paste problem. Because the upstream has not been
      // able to deal with this problem for a long time, it is
      // handled manually locally.
      // see: https://github.com/ueberdosis/tiptap/pull/3606
      new Plugin({
        key: new PluginKey("codeBlockVSCodeHandlerFixPaste"),
        props: {
          handlePaste: (view, event) => {
            if (!event.clipboardData) {
              return false;
            }
            // don’t create a new code block within code blocks
            if (this.editor.isActive(this.type.name)) {
              return false;
            }

            const text = event.clipboardData.getData("text/plain");
            const vscode = event.clipboardData.getData("vscode-editor-data");
            const vscodeData = vscode ? JSON.parse(vscode) : undefined;
            const language = vscodeData?.mode;

            if (!text || !language) {
              return false;
            }

            const { tr, schema } = view.state;

            // add text to code block
            // strip carriage return chars from text pasted as code
            // see: https://github.com/ProseMirror/prosemirror-view/commit/a50a6bcceb4ce52ac8fcc6162488d8875613aacd
            const contentTextNode = schema.text(text.replace(/\r\n?/g, "\n"));

            // create an empty code block
            tr.replaceSelectionWith(
              this.type.create({ language }, contentTextNode)
            );

            const { selection } = tr;
            // Whether the current position is code block, if not, move forward to code block.
            let codeBlockPos = Math.max(0, selection.from - 1);
            while (
              codeBlockPos > 0 &&
              tr.doc.resolve(codeBlockPos).parent.type.name !== this.type.name
            ) {
              codeBlockPos--;
            }
            // put cursor inside the newly created code block
            tr.setSelection(TextSelection.near(tr.doc.resolve(codeBlockPos)));

            // store meta information
            // this is useful for other plugins that depends on the paste event
            // like the paste rule plugin
            tr.setMeta("paste", true);

            view.dispatch(tr);

            return true;
          },
        },
      }),
      ...(this.parent?.() || []),
    ];
  },
});
