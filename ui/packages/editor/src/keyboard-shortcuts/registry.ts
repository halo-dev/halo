import type { Editor } from "@/tiptap";
import type {
  HaloKeyboardShortcut,
  HaloKeyboardShortcutDefinition,
  ResolvedHaloKeyboardShortcut,
} from "./types";

type Listener = () => void;

interface ShortcutRegistry {
  shortcuts: Map<string, HaloKeyboardShortcut>;
  listeners: Set<Listener>;
  helpListeners: Set<Listener>;
}

const registries = new WeakMap<Editor, ShortcutRegistry>();

function getRegistry(editor: Editor): ShortcutRegistry {
  let registry = registries.get(editor);
  if (!registry) {
    registry = {
      shortcuts: new Map(),
      listeners: new Set(),
      helpListeners: new Set(),
    };
    registries.set(editor, registry);
  }
  return registry;
}

export function registerHaloKeyboardShortcuts(
  editor: Editor,
  source: string,
  definitions: HaloKeyboardShortcutDefinition[]
) {
  const registry = getRegistry(editor);

  for (const definition of definitions) {
    registry.shortcuts.set(definition.id, {
      ...definition,
      keys: [...definition.keys],
      discoverable: definition.discoverable ?? true,
      priority: definition.priority ?? 100,
      source,
    });
  }

  registry.listeners.forEach((listener) => listener());
}

export function resolveHaloKeyboardShortcut(
  shortcut: HaloKeyboardShortcut
): ResolvedHaloKeyboardShortcut {
  return {
    ...shortcut,
    label:
      typeof shortcut.label === "function" ? shortcut.label() : shortcut.label,
    description:
      typeof shortcut.description === "function"
        ? shortcut.description()
        : shortcut.description,
  };
}

export function getHaloKeyboardShortcuts(
  editor: Editor
): ResolvedHaloKeyboardShortcut[] {
  return [...getRegistry(editor).shortcuts.values()]
    .filter(
      (shortcut) =>
        shortcut.discoverable !== false && (shortcut.visible?.(editor) ?? true)
    )
    .map(resolveHaloKeyboardShortcut)
    .sort(
      (left, right) =>
        (left.priority ?? 100) - (right.priority ?? 100) ||
        left.label.localeCompare(right.label)
    );
}

export function getHaloKeyboardShortcut(editor: Editor, id?: string) {
  if (!id) {
    return undefined;
  }
  const shortcut = getRegistry(editor).shortcuts.get(id);
  return shortcut ? resolveHaloKeyboardShortcut(shortcut) : undefined;
}

export function subscribeHaloKeyboardShortcuts(
  editor: Editor,
  listener: Listener
) {
  const listeners = getRegistry(editor).listeners;
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function requestHaloKeyboardShortcutHelp(editor: Editor) {
  getRegistry(editor).helpListeners.forEach((listener) => listener());
}

export function subscribeHaloKeyboardShortcutHelp(
  editor: Editor,
  listener: Listener
) {
  const listeners = getRegistry(editor).helpListeners;
  listeners.add(listener);
  return () => listeners.delete(listener);
}
