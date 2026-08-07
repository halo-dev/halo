import TiptapCodeBlock from "@tiptap/extension-code-block";
import { markRaw } from "vue";
import MingcuteBracesLine from "~icons/mingcute/braces-line";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  Editor,
  VueNodeViewRenderer,
  findParentNode,
  isActive,
  isNodeActive,
  posToDOMRect,
  type Range,
} from "@/tiptap";
import { EditorState, Plugin, PluginKey, TextSelection } from "@/tiptap/pm";
import type { ExtensionOptions } from "@/types";
import { deleteNode } from "@/utils";
import CodeBlockViewRenderer from "./CodeBlockViewRenderer.vue";
import { setCodeBlockWithIndent } from "./set-code-block-with-indent";

interface Option {
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
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
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

  /**
   * Use Tiptap's built-in Tab and Shift-Tab indentation inside code blocks.
   * @default true
   */
  enableTabIndentation: boolean | null | undefined;

  /**
   * Number of spaces inserted for code indentation.
   * @default 2
   */
  tabSize: number | null | undefined;
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

export const CODE_BLOCK_BUBBLE_MENU_KEY = new PluginKey("codeBlockBubbleMenu");

export const ExtensionCodeBlock = TiptapCodeBlock.extend<
  ExtensionOptions & Partial<ExtensionCodeBlockOptions>
>({
  // It needs to have a higher priority than range-selection (100),
  // otherwise the Mod-a shortcut key will be overridden.
  // But it should be lower than paragraph (1000) to avoid Backspace issues.
  priority: 101,

  fakeSelection: true,

  haloEditorIndentation: {
    keyboard: "passthrough",
  },

  addHaloEditorMetadata() {
    const languages = Array.isArray(this.options.languages)
      ? this.options.languages.map(({ value }) => value)
      : undefined;
    const themes = Array.isArray(this.options.themes)
      ? this.options.themes.map(({ value }) => value)
      : undefined;
    return {
      ai: {
        description:
          "A multi-line code block with optional language, syntax theme, and collapsed presentation.",
        aliases: ["fenced code block"],
        exposure: "recommended",
        useWhen: [
          "Presenting source code, configuration, terminal commands, or other preformatted text.",
        ],
        avoidWhen: ["The code fragment is short enough to remain inline."],
        contentGuidelines: [
          "Keep the code text verbatim and do not encode it as nested HTML.",
          "Set the language when it is known so a highlighting extension can render it correctly.",
        ],
        attributeGuidance: {
          language: {
            description:
              "Language identifier used for syntax highlighting and labeling.",
            ...(languages?.length ? { allowedValues: languages } : {}),
            examples: ["javascript", "java", "yaml", "bash"],
            omitWhen: ["The language cannot be determined reliably."],
          },
          collapsed: {
            description:
              "Whether the editor initially displays the code block in a collapsed state.",
            allowedValues: [true, false],
            omitWhen: ["The code should be visible by default."],
          },
          theme: {
            description:
              "Syntax-highlighting theme identifier. Plugins may extend the available themes.",
            ...(themes?.length ? { allowedValues: themes } : {}),
            examples: ["github-dark", "github-light"],
            omitWhen: ["The editor default theme should be used."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<pre><code>Plain preformatted text</code></pre>",
          '<pre><code class="language-javascript">const greeting = "Hello";</code></pre>',
          '<pre theme="github-dark" collapsed="true"><code class="language-typescript">const enabled: boolean = true;</code></pre>',
        ],
      },
    };
  },

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

  addKeyboardShortcuts() {
    return {
      ...this.parent?.(),
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
      "Mod-a": () => {
        if (this.editor.isActive(TiptapCodeBlock.name)) {
          const { tr, selection } = this.editor.state;
          const codeBlack = findParentNode(
            (node) => node.type.name === TiptapCodeBlock.name
          )(selection);
          if (!codeBlack) {
            return false;
          }
          const head = codeBlack.start;
          const anchor = codeBlack.start + codeBlack.node.content.size;
          if (selection.from === head && selection.to === anchor) {
            return false;
          }
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
      enableTabIndentation: true,
      tabSize: 2,
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 160,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive(TiptapCodeBlock.name),
            icon: markRaw(MingcuteBracesLine),
            title: i18n.global.t("editor.common.codeblock.title"),
            action: () => {
              if (editor.isActive(TiptapCodeBlock.name)) {
                editor.chain().focus().toggleCodeBlock().run();
                return;
              }
              setCodeBlockWithIndent(editor);
            },
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 80,
          icon: markRaw(MingcuteBracesLine),
          title: "editor.common.codeblock.title",
          keywords: ["codeblock", "daimakuai"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            setCodeBlockWithIndent(editor, range);
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 60,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(MingcuteBracesLine),
              title: i18n.global.t("editor.common.codeblock.title"),
              action: () => {
                setCodeBlockWithIndent(editor);
              },
            },
          },
        ];
      },
      getBubbleMenu() {
        return {
          pluginKey: CODE_BLOCK_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, TiptapCodeBlock.name);
          },
          options: {
            placement: "top-start",
          },
          getReferencedVirtualElement() {
            const editor = this.editor;
            if (!editor) {
              return null;
            }
            const parentNode = findParentNode(
              (node) => node.type.name === ExtensionCodeBlock.name
            )(editor.state.selection);
            if (parentNode) {
              const domRect = posToDOMRect(
                editor.view,
                parentNode.pos,
                parentNode.pos + parentNode.node.nodeSize
              );
              return {
                getBoundingClientRect: () => domRect,
                getClientRects: () => [domRect],
              };
            }
            return null;
          },
          items: [
            {
              priority: 10,
              props: {
                icon: markRaw(MingcuteDelete2Line),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }: { editor: Editor }) =>
                  deleteNode(TiptapCodeBlock.name, editor),
              },
            },
          ],
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
