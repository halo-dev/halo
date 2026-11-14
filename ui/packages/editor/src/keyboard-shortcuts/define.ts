import type { KeyboardShortcutCommand } from "@/tiptap";
import { registerHaloKeyboardShortcutBindings } from "./bindings";
import { registerHaloKeyboardShortcuts } from "./registry";
import type {
  HaloKeyboardShortcutContext,
  HaloKeyboardShortcutDefinition,
} from "./types";

function warnMissingCommand(context: HaloKeyboardShortcutContext, key: string) {
  if (!import.meta.env.DEV) {
    return;
  }
  console.warn(
    `[Halo editor] Shortcut "${key}" from "${context.name}" has no command.`
  );
}

/**
 * Enriches Tiptap's native `addKeyboardShortcuts` return value with Halo
 * metadata. Extensions still expose one hook while Halo can render tooltips
 * and a complete, editor-scoped shortcut reference.
 */
export function defineHaloKeyboardShortcuts(
  context: HaloKeyboardShortcutContext,
  definitions: HaloKeyboardShortcutDefinition[]
): Record<string, KeyboardShortcutCommand> {
  const inherited = context.parent?.() ?? {};
  const shortcuts = { ...inherited };
  const registeredDefinitions: HaloKeyboardShortcutDefinition[] = [];
  const registeredBindings: {
    id: string;
    key: string;
    command: KeyboardShortcutCommand;
  }[] = [];

  for (const definition of definitions) {
    const registeredKeys: string[] = [];
    for (const key of definition.keys) {
      const command = definition.command ?? inherited[key];
      if (!command) {
        warnMissingCommand(context, key);
        continue;
      }
      shortcuts[key] = command;
      registeredKeys.push(key);
      registeredBindings.push({
        id: definition.id,
        key,
        command,
      });
    }

    if (registeredKeys.length) {
      registeredDefinitions.push({
        ...definition,
        keys: registeredKeys,
      });
    }
  }

  registerHaloKeyboardShortcuts(
    context.editor,
    context.name,
    registeredDefinitions
  );
  registerHaloKeyboardShortcutBindings(
    context.editor,
    context.name,
    registeredBindings
  );
  return shortcuts;
}
