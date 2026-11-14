/**
 * Keyboard shortcut utilities
 * Reference: tiptap keyboard shortcuts format https://tiptap.dev/docs/editor/core-concepts/keyboard-shortcuts
 */

export interface ParsedShortcut {
  key: string;
  ctrlKey: boolean;
  shiftKey: boolean;
  altKey: boolean;
  metaKey: boolean;
}

type ShortcutModifierFlag = Exclude<keyof ParsedShortcut, "key">;

const IMPLICIT_SHIFT_KEYS = new Set([
  "~",
  "!",
  "@",
  "#",
  "$",
  "%",
  "^",
  "&",
  "*",
  "(",
  ")",
  "_",
  "+",
  "{",
  "}",
  "|",
  ":",
  '"',
  "<",
  ">",
  "?",
]);

const PHYSICAL_KEY_CODES: Record<string, string[]> = {
  " ": ["Space"],
  "+": ["Equal", "NumpadAdd"],
  "=": ["Equal", "NumpadEqual"],
  "-": ["Minus", "NumpadSubtract"],
  _: ["Minus", "NumpadSubtract"],
  "/": ["Slash", "NumpadDivide"],
  "?": ["Slash"],
  "[": ["BracketLeft"],
  "{": ["BracketLeft"],
  "]": ["BracketRight"],
  "}": ["BracketRight"],
  "\\": ["Backslash"],
  "|": ["Backslash"],
  ",": ["Comma"],
  "<": ["Comma"],
  ".": ["Period", "NumpadDecimal"],
  ">": ["Period"],
  ";": ["Semicolon"],
  ":": ["Semicolon"],
  "'": ["Quote"],
  '"': ["Quote"],
  "`": ["Backquote"],
  "~": ["Backquote"],
};

const KEY_ALIASES: Record<string, string> = {
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

const MODIFIER_FLAGS: Record<string, ShortcutModifierFlag> = {
  ctrl: "ctrlKey",
  control: "ctrlKey",
  shift: "shiftKey",
  alt: "altKey",
  option: "altKey",
  cmd: "metaKey",
  command: "metaKey",
  meta: "metaKey",
};

const MAC_MODIFIER_LABELS: Array<[ShortcutModifierFlag, string]> = [
  ["ctrlKey", "⌃"],
  ["altKey", "⌥"],
  ["shiftKey", "⇧"],
  ["metaKey", "⌘"],
];

const OTHER_MODIFIER_LABELS: Array<[ShortcutModifierFlag, string]> = [
  ["ctrlKey", "Ctrl"],
  ["altKey", "Alt"],
  ["shiftKey", "Shift"],
  ["metaKey", "Meta"],
];

const KEY_DISPLAY_LABELS: Record<string, string> = {
  " ": "Space",
  Enter: "↵",
  Delete: "Del",
};

function getPlatform() {
  if (typeof navigator === "undefined") {
    return "";
  }

  const userAgentData = (
    navigator as Navigator & { userAgentData?: { platform?: string } }
  ).userAgentData;
  return userAgentData?.platform || navigator.platform;
}

/**
 * Check if the current platform is Mac
 */
export const isMac = () => {
  return /Mac|iPhone|iPad|iPod/i.test(getPlatform());
};

const isWindows = () => {
  return /Win/i.test(getPlatform());
};

function setShortcutModifier(result: ParsedShortcut, modifier: string) {
  if (modifier === "mod") {
    result[isMac() ? "metaKey" : "ctrlKey"] = true;
    return;
  }

  const flag = MODIFIER_FLAGS[modifier];
  if (flag) {
    result[flag] = true;
  }
}

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
  const parts = shortcut.split(/-(?!$)/);
  const rawKey = parts.pop() ?? "";
  const result: ParsedShortcut = {
    key: normalizeKey(rawKey),
    ctrlKey: false,
    shiftKey: false,
    altKey: false,
    metaKey: false,
  };

  for (const part of parts) {
    setShortcutModifier(result, part.toLowerCase());
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
  const lowerKey = key.toLowerCase();
  if (KEY_ALIASES[lowerKey]) {
    return KEY_ALIASES[lowerKey];
  }

  if (key.length === 1) {
    return key.toLowerCase();
  }

  return key;
}

function modifiersMatch(event: KeyboardEvent, shortcut: ParsedShortcut) {
  return (
    event.ctrlKey === shortcut.ctrlKey &&
    event.altKey === shortcut.altKey &&
    event.metaKey === shortcut.metaKey
  );
}

function normalizeEventKey(key: string) {
  if (key === " ") {
    return " ";
  }
  return key.toLowerCase();
}

function getExpectedPhysicalKeyCodes(key: string) {
  if (/^[a-z]$/.test(key)) {
    return [`Key${key.toUpperCase()}`];
  }
  if (/^\d$/.test(key)) {
    return [`Digit${key}`, `Numpad${key}`];
  }
  return PHYSICAL_KEY_CODES[key] ?? [key];
}

function physicalKeyMatches(event: KeyboardEvent, shortcutKey: string) {
  const isWindowsAltGraph = isWindows() && event.ctrlKey && event.altKey;
  if (!event.altKey || !event.code || isWindowsAltGraph) {
    return false;
  }

  return getExpectedPhysicalKeyCodes(shortcutKey).includes(event.code);
}

function shortcutShiftMatches(
  event: KeyboardEvent,
  shortcut: ParsedShortcut,
  rawKey: string,
  eventKeyMatches: boolean,
  matchesByPhysicalKey: boolean
) {
  if (shortcut.shiftKey) {
    return event.shiftKey;
  }

  const usesUppercaseKey = /^[A-Z]$/.test(rawKey);
  const usesImplicitShift =
    IMPLICIT_SHIFT_KEYS.has(shortcut.key) || usesUppercaseKey;
  if (!usesImplicitShift) {
    return !event.shiftKey;
  }
  if (usesUppercaseKey) {
    return event.shiftKey;
  }

  // Option/Alt can change event.key according to the active keyboard layout.
  // In that case, the shortcut names the physical symbol key and does not
  // require users to press Shift as an additional modifier.
  return eventKeyMatches || event.shiftKey || matchesByPhysicalKey;
}

/**
 * Check if a keyboard event matches a shortcut
 * @param event - Keyboard event
 * @param shortcut - Shortcut string
 * @returns Whether the event matches the shortcut
 */
export function matchShortcut(event: KeyboardEvent, shortcut: string): boolean {
  const parsed = parseShortcut(shortcut);
  if (!modifiersMatch(event, parsed)) {
    return false;
  }

  const eventKey = normalizeEventKey(event.key);
  const eventKeyMatches = eventKey === parsed.key.toLowerCase();
  const matchesByPhysicalKey =
    !eventKeyMatches && physicalKeyMatches(event, parsed.key.toLowerCase());
  if (!eventKeyMatches && !matchesByPhysicalKey) {
    return false;
  }

  const shortcutParts = shortcut.split(/-(?!$)/);
  const rawKey = shortcutParts[shortcutParts.length - 1] ?? "";
  return shortcutShiftMatches(
    event,
    parsed,
    rawKey,
    eventKeyMatches,
    matchesByPhysicalKey
  );
}

/**
 * Format shortcut to display text
 * @param shortcut - Shortcut string
 * @returns Formatted display text
 */
export function formatShortcut(shortcut: string): string {
  const parts = formatShortcutParts(shortcut);
  const separator = isMac() ? "" : "+";
  return parts.join(separator);
}

function getModifierLabels(shortcut: ParsedShortcut) {
  const labels = isMac() ? MAC_MODIFIER_LABELS : OTHER_MODIFIER_LABELS;
  return labels.filter(([flag]) => shortcut[flag]).map(([, label]) => label);
}

function getKeyDisplayLabel(key: string) {
  const label = KEY_DISPLAY_LABELS[key] ?? key;
  if (label.length === 1) {
    return label.toUpperCase();
  }
  return label;
}

/**
 * Format a shortcut as individual keycaps.
 */
export function formatShortcutParts(shortcut: string): string[] {
  const parsed = parseShortcut(shortcut);
  return [...getModifierLabels(parsed), getKeyDisplayLabel(parsed.key)];
}
