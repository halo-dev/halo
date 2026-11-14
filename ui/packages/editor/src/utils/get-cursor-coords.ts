import type { EditorView } from "@/tiptap";

export type Coords = {
  top: number;
  bottom: number;
  left: number;
  right: number;
};

export type NestedCursorCoords = {
  coords: Coords;
  startDOM: Node;
};

const toCoords = (r: DOMRect): Coords => ({
  top: r.top,
  bottom: r.bottom,
  left: r.left,
  right: r.right,
});

/**
 * Get the actual cursor coordinates.
 *
 * `coordsAtPos` only knows about ProseMirror's own node tree. A verified DOM
 * selection inside a nested contenteditable gives us a more precise caret
 * position; all other focus states safely fall back to ProseMirror.
 */
export const getCursorCoords = (view: EditorView): Coords | null => {
  return (
    getNestedCursorCoords(view)?.coords ??
    view.coordsAtPos(view.state.selection.$head.pos)
  );
};

/**
 * Return a caret only when the browser selection is owned by the focused
 * nested contenteditable. Native inputs and NodeView controls are deliberately
 * excluded because `window.getSelection()` does not describe their caret.
 */
export const getNestedCursorCoords = (
  view: EditorView
): NestedCursorCoords | null => {
  const activeEl = document.activeElement;
  if (!(activeEl instanceof HTMLElement)) {
    return null;
  }
  if (activeEl === view.dom || !view.dom.contains(activeEl)) {
    return null;
  }
  if (isNativeControl(activeEl)) {
    return null;
  }

  const editableHost = findNestedEditableHost(activeEl, view.dom);
  if (!editableHost) {
    return null;
  }

  const sel = window.getSelection();
  if (!sel || sel.rangeCount === 0) {
    return null;
  }

  const range = sel.getRangeAt(0);
  if (
    !editableHost.contains(range.startContainer) ||
    !editableHost.contains(range.endContainer)
  ) {
    return null;
  }

  const coords = getRangeCoords(range);
  return coords ? { coords, startDOM: range.startContainer } : null;
};

const getRangeCoords = (range: Range): Coords | null => {
  // Some nested editors expose a non-zero rectangle for a collapsed caret.
  const rect = range.getBoundingClientRect();
  if (hasSize(rect)) {
    return toCoords(rect);
  }

  // Other browsers only expose a client rect for collapsed ranges.
  for (const r of range.getClientRects()) {
    if (hasSize(r)) {
      return toCoords(r);
    }
  }

  if (range.collapsed) {
    const nodeType = range.startContainer.nodeType;
    if (nodeType === Node.TEXT_NODE) {
      const textNode = range.startContainer as Text;
      const offset = range.startOffset;
      const charRange = document.createRange();
      if (offset > 0) {
        charRange.setStart(textNode, offset - 1);
        charRange.setEnd(textNode, offset);
      } else if (textNode.length > 0) {
        charRange.setStart(textNode, 0);
        charRange.setEnd(textNode, 1);
      }
      if (!charRange.collapsed) {
        const r = charRange.getBoundingClientRect();
        if (hasSize(r)) {
          return toCoords(r);
        }
      }
    }

    // Empty lines often place the range on a line element or its <br>.
    if (nodeType === Node.ELEMENT_NODE) {
      const el = range.startContainer as Element;
      const child = el.childNodes[range.startOffset] as Element | undefined;
      if (child && typeof child.getBoundingClientRect === "function") {
        const r = child.getBoundingClientRect();
        if (hasSize(r)) {
          return toCoords(r);
        }
      }
      const r = el.getBoundingClientRect();
      if (hasSize(r)) {
        return toCoords(r);
      }
    }
  }

  return null;
};

const findNestedEditableHost = (
  activeElement: HTMLElement,
  editorRoot: HTMLElement
): HTMLElement | null => {
  for (
    let element: HTMLElement | null = activeElement;
    element && element !== editorRoot;
    element = element.parentElement
  ) {
    const contentEditable = element
      .getAttribute("contenteditable")
      ?.toLowerCase();
    if (
      contentEditable === "" ||
      contentEditable === "true" ||
      contentEditable === "plaintext-only"
    ) {
      return element;
    }
    if (contentEditable === "false") {
      return null;
    }
  }
  return null;
};

const isNativeControl = (element: HTMLElement): boolean =>
  element.matches("button, input, select, textarea");

const hasSize = (rect: Pick<DOMRect, "height" | "width">): boolean =>
  rect.height > 0 || rect.width > 0;
