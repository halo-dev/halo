import {
  type CommandProps,
  type Extensions,
  type KeyboardShortcutCommand,
  Extension,
  isList,
  CoreEditor,
} from "@/tiptap";
import { TextSelection, Transaction } from "@/tiptap/pm";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    indent: {
      indent: () => ReturnType;
      outdent: () => ReturnType;
    };
  }
}

type IndentOptions = {
  names: Array<string>;
  indentRange: number;
  minIndentLevel: number;
  maxIndentLevel: number;
  defaultIndentLevel: number;
  HTMLAttributes: Record<string, any>;
};
const Indent = Extension.create<IndentOptions, never>({
  name: "indent",

  addOptions() {
    return {
      names: ["heading", "paragraph"],
      indentRange: 24,
      minIndentLevel: 0,
      maxIndentLevel: 24 * 10,
      defaultIndentLevel: 0,
      HTMLAttributes: {},
    };
  },

  addGlobalAttributes() {
    return [
      {
        types: this.options.names,
        attributes: {
          indent: {
            default: this.options.defaultIndentLevel,
            renderHTML: (attributes) => ({
              style:
                attributes.indent != 0
                  ? `margin-left: ${attributes.indent}px!important;`
                  : "",
            }),
            parseHTML: (element) =>
              parseInt(element.style.marginLeft, 10) ||
              this.options.defaultIndentLevel,
          },
        },
      },
    ];
  },

  addCommands(this) {
    return {
      indent:
        () =>
        ({ tr, state, dispatch, editor }: CommandProps) => {
          const { selection } = state;
          tr = tr.setSelection(selection);
          tr = updateIndentLevel(
            tr,
            this.options,
            editor.extensionManager.extensions,
            "indent"
          );
          if (tr.docChanged && dispatch) {
            dispatch(tr);
            return true;
          }
          return false;
        },
      outdent:
        () =>
        ({ tr, state, dispatch, editor }: CommandProps) => {
          const { selection } = state;
          tr = tr.setSelection(selection);
          tr = updateIndentLevel(
            tr,
            this.options,
            editor.extensionManager.extensions,
            "outdent"
          );
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
      Tab: getIndent(),
      "Shift-Tab": getOutdent(false),
      "Mod-]": getIndent(),
      "Mod-[": getOutdent(false),
    };
  },

  onUpdate() {
    const { editor } = this;
    if (editor.isActive("listItem")) {
      const node = editor.state.selection.$head.node();
      if (node.attrs.indent) {
        editor.commands.updateAttributes(node.type.name, { indent: 0 });
      }
    }
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
  delta: number,
  min: number,
  max: number
): Transaction {
  if (!tr.doc) return tr;
  const node = tr.doc.nodeAt(pos);
  if (!node) return tr;
  const indent = clamp((node.attrs.indent || 0) + delta, min, max);
  if (indent === node.attrs.indent) return tr;
  const nodeAttrs = {
    ...node.attrs,
    indent,
  };
  return tr.setNodeMarkup(pos, node.type, nodeAttrs, node.marks);
}

type IndentType = "indent" | "outdent";
const updateIndentLevel = (
  tr: Transaction,
  options: IndentOptions,
  extensions: Extensions,
  type: IndentType
): Transaction => {
  const { doc, selection } = tr;
  if (!doc || !selection) return tr;
  if (!(selection instanceof TextSelection)) {
    return tr;
  }
  const { from, to } = selection;
  doc.nodesBetween(from, to, (node, pos) => {
    if (options.names.includes(node.type.name)) {
      if (isTextIndent(tr, pos) && type === "indent") {
        tr.insertText("\t", from, to);
      } else {
        tr = setNodeIndentMarkup(
          tr,
          pos,
          options.indentRange * (type === "indent" ? 1 : -1),
          options.minIndentLevel,
          options.maxIndentLevel
        );
      }
      return false;
    }
    return !isList(node.type.name, extensions);
  });

  return tr;
};

const isTextIndent = (tr: Transaction, currNodePos: number) => {
  const { selection } = tr;
  const { from, to } = selection;
  if (from == 0) {
    return false;
  }
  if (from - to == 0 && currNodePos != from - 1) {
    return true;
  }
  return false;
};

const isListActive = (editor: CoreEditor) => {
  return (
    editor.isActive("bulletList") ||
    editor.isActive("orderedList") ||
    editor.isActive("taskList")
  );
};

const isFilterActive = (editor: CoreEditor) => {
  return editor.isActive("table") || editor.isActive("columns");
};

export const getIndent: () => KeyboardShortcutCommand =
  () =>
  ({ editor }) => {
    // @ts-ignore
    if (isFilterActive(editor)) {
      return false;
    }
    // @ts-ignore
    if (isListActive(editor)) {
      const name = editor.can().sinkListItem("listItem")
        ? "listItem"
        : "taskItem";
      return editor.chain().focus().sinkListItem(name).run();
    }
    return editor.chain().focus().indent().run();
  };
export const getOutdent: (
  outdentOnlyAtHead: boolean
) => KeyboardShortcutCommand =
  (outdentOnlyAtHead) =>
  ({ editor }) => {
    if (outdentOnlyAtHead && editor.state.selection.$head.parentOffset > 0) {
      return false;
    }

    if (isFilterActive(editor)) {
      return false;
    }

    if (isListActive(editor)) {
      const name = editor.can().liftListItem("listItem")
        ? "listItem"
        : "taskItem";
      return editor.chain().focus().liftListItem(name).run();
    }
    return editor.chain().focus().outdent().run();
  };

export default Indent;
