/**
 * Keyboard shortcut utilities
 * Reference: tiptap keyboard shortcuts format https://tiptap.dev/docs/editor/core-concepts/keyboard-shortcuts
 */

interface ParsedShortcut {
  key: string;
  ctrlKey: boolean;
  shiftKey: boolean;
  altKey: boolean;
  metaKey: boolean;
}

/**
 * Check if the current platform is Mac
 */
const isMac = () =>
  typeof navigator !== "undefined" && /Mac/.test(navigator.platform);

/**
 * Parse shortcut string
 * Supported formats:
 * - Mod-l (Mac: Cmd+L, Others: Ctrl+L)
 * - Control-Shift-Enter
 * - Shift-Tab
 * - Alt-1
 *
 * @param shortcut - Shortcut string
 * @returns Parsed shortcut object
 */
export function parseShortcut(shortcut: string): ParsedShortcut {
  const parts = shortcut.split("-");
  const result: ParsedShortcut = {
    key: "",
    ctrlKey: false,
    shiftKey: false,
    altKey: false,
    metaKey: false,
  };

  for (let i = 0; i < parts.length; i++) {
    const part = parts[i];

    if (i === parts.length - 1) {
      result.key = normalizeKey(part);
      continue;
    }

    const modifier = part.toLowerCase();
    switch (modifier) {
      case "mod":
        if (isMac()) {
          result.metaKey = true;
        } else {
          result.ctrlKey = true;
        }
        break;
      case "ctrl":
      case "control":
        result.ctrlKey = true;
        break;
      case "shift":
        result.shiftKey = true;
        break;
      case "alt":
      case "option":
        result.altKey = true;
        break;
      case "cmd":
      case "command":
      case "meta":
        result.metaKey = true;
        break;
    }
  }

  return result;
}

/**
 * Normalize key name
 *
 * @param key - Key string
 * @returns Normalized key name
 */
function normalizeKey(key: string): string {
  const keyMap: Record<string, string> = {
    space: " ",
    enter: "Enter",
    tab: "Tab",
    backspace: "Backspace",
    delete: "Delete",
    escape: "Escape",
    esc: "Escape",
    arrowleft: "ArrowLeft",
    arrowright: "ArrowRight",
    arrowup: "ArrowUp",
    arrowdown: "ArrowDown",
  };

  const lowerKey = key.toLowerCase();
  if (keyMap[lowerKey]) {
    return keyMap[lowerKey];
  }

  if (key.length === 1) {
    return key.toLowerCase();
  }

  return key;
}

/**
 * Check if a keyboard event matches a shortcut
 * @param event - Keyboard event
 * @param shortcut - Shortcut string
 * @returns Whether the event matches the shortcut
 */
export function matchShortcut(event: KeyboardEvent, shortcut: string): boolean {
  const parsed = parseShortcut(shortcut);

  let eventKey = event.key.toLowerCase();

  if (event.key === " ") {
    eventKey = " ";
  }

  if (eventKey !== parsed.key.toLowerCase()) {
    return false;
  }

  if (event.ctrlKey !== parsed.ctrlKey) {
    return false;
  }
  if (event.shiftKey !== parsed.shiftKey) {
    return false;
  }
  if (event.altKey !== parsed.altKey) {
    return false;
  }
  if (event.metaKey !== parsed.metaKey) {
    return false;
  }

  return true;
}

/**
 * Format shortcut to display text
 * @param shortcut - Shortcut string
 * @returns Formatted display text
 */
export function formatShortcut(shortcut: string): string {
  const parsed = parseShortcut(shortcut);
  const parts: string[] = [];

  if (isMac()) {
    if (parsed.ctrlKey) {
      parts.push("⌃");
    }
    if (parsed.altKey) {
      parts.push("⌥");
    }
    if (parsed.shiftKey) {
      parts.push("⇧");
    }
    if (parsed.metaKey) {
      parts.push("⌘");
    }
  } else {
    if (parsed.ctrlKey) {
      parts.push("Ctrl");
    }
    if (parsed.altKey) {
      parts.push("Alt");
    }
    if (parsed.shiftKey) parts.push("Shift");
    if (parsed.metaKey) {
      parts.push("Meta");
    }
  }

  let key = parsed.key;
  switch (key) {
    case " ":
      key = "Space";
      break;
    case "Enter":
      key = "↵";
      break;
    case "Delete":
      key = "Del";
      break;
  }
  if (key.length === 1) {
    key = key.toUpperCase();
  }
  parts.push(key);

  return isMac() ? parts.join("") : parts.join("+");
}
