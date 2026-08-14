import type { Editor, KeyboardShortcutCommand } from "@/tiptap";

interface HaloKeyboardShortcutBinding {
  id: string;
  key: string;
  command: KeyboardShortcutCommand;
}

const bindingsByEditor = new WeakMap<
  Editor,
  Map<string, HaloKeyboardShortcutBinding[]>
>();

function getBindingsBySource(editor: Editor) {
  let bindings = bindingsByEditor.get(editor);
  if (!bindings) {
    bindings = new Map();
    bindingsByEditor.set(editor, bindings);
  }
  return bindings;
}

export function registerHaloKeyboardShortcutBindings(
  editor: Editor,
  source: string,
  bindings: HaloKeyboardShortcutBinding[]
) {
  getBindingsBySource(editor).set(source, bindings);
}

export function getHaloKeyboardShortcutBindings(editor: Editor) {
  return [...getBindingsBySource(editor).values()].flat();
}
