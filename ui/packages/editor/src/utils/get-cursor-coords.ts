import type { EditorView } from "@/tiptap";

export type Coords = {
  top: number;
  bottom: number;
  left: number;
  right: number;
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
 * coordsAtPos only knows about ProseMirror's own node tree. When the focused
 * element is inside a NodeView that hosts its own editor (CodeMirror, Monaco,
 * a plain <textarea>, …), coordsAtPos returns the bounding box of the NodeView
 * container, not the real caret position.
 *
 * Detection: if document.activeElement is inside the editor DOM but is NOT
 * ProseMirror's own contenteditable (view.dom), we are in a nested editor.
 */
export const getCursorCoords = (view: EditorView): Coords | null => {
  const pmCoords = view.coordsAtPos(view.state.selection.$head.pos);
  const activeEl = document.activeElement;
  if (!activeEl) {
    return pmCoords;
  }

  if (activeEl === view.dom || !view.dom.contains(activeEl)) {
    return pmCoords;
  }

  // Use the native Selection API to get the cursor coordinates.
  const sel = window.getSelection();
  if (!sel || sel.rangeCount === 0) {
    return null;
  }

  const range = sel.getRangeAt(0);

  // 1. Use getBoundingClientRect to get the cursor coordinates.
  const rect = range.getBoundingClientRect();
  if (rect.height > 0 || rect.width > 0) {
    return toCoords(rect);
  }

  // 2. Use getClientRects to get the cursor coordinates
  // some browsers populate this for collapsed ranges even when getBoundingClientRect returns zero.
  for (const r of range.getClientRects()) {
    if (r.height > 0 || r.width > 0) {
      return toCoords(r);
    }
  }

  // 3. Collapsed caret
  if (range.collapsed) {
    // 3.1. Collapsed caret inside a text node
    // Try to measure an adjacent character. It sits on the same line, so its vertical metrics match.
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
        if (r.height > 0) {
          return toCoords(r);
        }
      }
    }

    // 3.2. Collapsed caret inside an element node
    // This happens on empty lines where the DOM is e.g. <div class="cm-line"><br></div> and range.startContainer is the div rather than a text node.
    // Try the child node at the caret offset first (often the <br>), then fall back to the container element itself.
    if (nodeType === Node.ELEMENT_NODE) {
      const el = range.startContainer as Element;
      const child = el.childNodes[range.startOffset] as Element | undefined;
      if (child && typeof child.getBoundingClientRect === "function") {
        const r = child.getBoundingClientRect();
        if (r.height > 0) {
          return toCoords(r);
        }
      }
      const r = el.getBoundingClientRect();
      if (r.height > 0) {
        return toCoords(r);
      }
    }
  }

  return null;
};
