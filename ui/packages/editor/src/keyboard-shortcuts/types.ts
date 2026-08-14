import type { Editor, KeyboardShortcutCommand } from "@/tiptap";

export type HaloKeyboardShortcutCategory =
  | "general"
  | "formatting"
  | "structure"
  | "navigation";

export type HaloKeyboardShortcutLabel = string | (() => string);

export interface HaloKeyboardShortcutDefinition {
  /** Stable identifier used by tooltips and the shortcut help panel. */
  id: string;
  /** Tiptap-compatible key combinations. The first one is the primary shortcut. */
  keys: string[];
  label: HaloKeyboardShortcutLabel;
  category: HaloKeyboardShortcutCategory;
  description?: HaloKeyboardShortcutLabel;
  discoverable?: boolean;
  priority?: number;
  visible?: (editor: Editor) => boolean;
  /**
   * Optional command implementation. When omitted, the matching command from
   * the parent Tiptap extension is retained.
   */
  command?: KeyboardShortcutCommand;
}

export interface HaloKeyboardShortcut extends Omit<
  HaloKeyboardShortcutDefinition,
  "command"
> {
  source: string;
}

export interface ResolvedHaloKeyboardShortcut extends Omit<
  HaloKeyboardShortcut,
  "label" | "description"
> {
  label: string;
  description?: string;
}

export interface HaloKeyboardShortcutContext {
  editor: Editor;
  name: string;
  parent?: () => Record<string, KeyboardShortcutCommand>;
}
