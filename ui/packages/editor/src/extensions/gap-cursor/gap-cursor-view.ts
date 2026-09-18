import type { EditorState } from "@/tiptap";
import {
  Decoration,
  DecorationSet,
  GapCursor,
  type EditorView,
} from "@/tiptap/pm";
import {
  getEditorNodeElement,
  getGapCursorTarget,
  isGapCursorTargetNode,
  type GapCursorSide,
} from "@/utils";
import {
  getGapCursorSelectionSide,
  HaloGapCursor,
} from "./gap-cursor-selection";

const VERTICAL_GAP_CLICK_DISTANCE = 24;
const GAP_CURSOR_CORNER_CLICK_DISTANCE = 12;
const INTERACTIVE_ELEMENT_SELECTOR = [
  "button",
  "a",
  "input",
  "select",
  "textarea",
  '[role="button"]',
  "[data-drag-handle]",
  ".grip-row",
  ".grip-column",
  ".grip-table",
  ".cm-editor",
].join(", ");

interface GapCursorMouseHit {
  pos: number;
  side: GapCursorSide;
  distance: number;
  source: "corner" | "gutter";
}

export function handleGapCursorMouseDown(
  view: EditorView,
  event: MouseEvent
): boolean {
  return handleGapCursorMouseDownFrom(view, event);
}

function handleGapCursorMouseDownFrom(
  view: EditorView,
  event: MouseEvent,
  source?: GapCursorMouseHit["source"]
): boolean {
  if (!canHandleMouseDown(view, event)) {
    return false;
  }

  const hit = findGapCursorMouseHit(view, event);
  if (!hit || (source && hit.source !== source)) {
    return false;
  }

  event.preventDefault();
  view.focus();
  const $pos = view.state.doc.resolve(hit.pos);
  view.dispatch(view.state.tr.setSelection(new HaloGapCursor($pos, hit.side)));
  return true;
}

export function drawGapCursor(state: EditorState) {
  if (!(state.selection instanceof GapCursor)) {
    return null;
  }

  const side = getGapCursorSelectionSide(state.selection);
  if (!side) {
    return null;
  }

  return DecorationSet.create(state.doc, [
    Decoration.widget(
      state.selection.head,
      () => {
        const cursor = document.createElement("div");
        cursor.className = `ProseMirror-gapcursor halo-gap-cursor--${side}`;
        return cursor;
      },
      {
        key: `halo-gap-cursor-${side}`,
        side: side === "before" ? -1 : 1,
      }
    ),
  ]);
}

export class GapCursorPositioner {
  private view: EditorView;

  private resizeObserver?: ResizeObserver;

  private observedTarget?: HTMLElement;

  constructor(view: EditorView) {
    this.view = view;
    view.dom.addEventListener("mousedown", this.handleCornerMouseDown, true);
    if (typeof ResizeObserver !== "undefined") {
      this.resizeObserver = new ResizeObserver(this.position);
      this.resizeObserver.observe(view.dom);
    }
    window.addEventListener("resize", this.position);
    this.position();
  }

  update(view: EditorView) {
    this.view = view;
    this.position();
  }

  destroy() {
    this.resizeObserver?.disconnect();
    this.view.dom.removeEventListener(
      "mousedown",
      this.handleCornerMouseDown,
      true
    );
    window.removeEventListener("resize", this.position);
  }

  private handleCornerMouseDown = (event: MouseEvent) => {
    if (!isPotentialCapturedCornerMouseDown(this.view, event)) {
      return;
    }
    if (!handleGapCursorMouseDownFrom(this.view, event, "corner")) {
      return;
    }
    event.stopImmediatePropagation();
  };

  private position = () => {
    const { selection } = this.view.state;
    if (!(selection instanceof GapCursor)) {
      return;
    }

    const side = getGapCursorSelectionSide(selection);
    if (!side) {
      return;
    }

    const target = getGapCursorTarget(selection.$from, side);
    if (!target) {
      return;
    }

    const cursor = this.view.dom.querySelector<HTMLElement>(
      `.halo-gap-cursor--${side}`
    );
    if (!cursor) {
      return;
    }

    const element = getEditorNodeElement(this.view, target.pos);
    if (!element) {
      return;
    }

    const visualElement = getGapCursorVisualElement(element);
    this.observeTarget(visualElement);
    positionCursor(cursor, visualElement, side);
  };

  private observeTarget(target: HTMLElement) {
    if (!this.resizeObserver) {
      return;
    }
    if (this.observedTarget === target) {
      return;
    }
    if (this.observedTarget) {
      this.resizeObserver.unobserve(this.observedTarget);
    }
    this.observedTarget = target;
    this.resizeObserver.observe(target);
  }
}

function canHandleMouseDown(view: EditorView, event: MouseEvent): boolean {
  if (!view.editable) {
    return false;
  }
  if (event.button !== 0) {
    return false;
  }

  const hasModifier =
    event.altKey || event.ctrlKey || event.metaKey || event.shiftKey;
  return !hasModifier;
}

function isPotentialCapturedCornerMouseDown(
  view: EditorView,
  event: MouseEvent
): boolean {
  if (!canHandleMouseDown(view, event)) {
    return false;
  }

  const eventElement = event.target instanceof Element ? event.target : null;
  if (!eventElement || eventElement.closest(INTERACTIVE_ELEMENT_SELECTOR)) {
    return false;
  }

  const nodeViewElement = eventElement.closest<HTMLElement>(
    "[data-node-view-wrapper], [data-gap-cursor-click-area]"
  );
  if (!nodeViewElement || !view.dom.contains(nodeViewElement)) {
    return false;
  }

  const rect =
    getGapCursorVisualElement(nodeViewElement).getBoundingClientRect();
  return isPointInsideBeforeCorner(event.clientX, event.clientY, rect);
}

function findGapCursorMouseHit(
  view: EditorView,
  event: MouseEvent
): GapCursorMouseHit | null {
  const editorRect = view.dom.getBoundingClientRect();
  const { clientX: x, clientY: y } = event;
  if (!isPointInsideRect(x, y, editorRect)) {
    return null;
  }

  const eventElement = event.target instanceof Element ? event.target : null;
  if (eventElement?.closest(INTERACTIVE_ELEMENT_SELECTOR)) {
    return null;
  }

  const hits: GapCursorMouseHit[] = [];
  view.state.doc.descendants((node, pos) => {
    if (!isGapCursorTargetNode(node)) {
      return true;
    }

    const element = getEditorNodeElement(view, pos);
    if (!element) {
      return false;
    }
    if (!isGapCursorClickArea(view, event.target, eventElement, element)) {
      return false;
    }

    const visualElement = getGapCursorVisualElement(element);
    const rect = visualElement.getBoundingClientRect();
    addBeforeCornerHit(hits, pos, x, y, rect);
    if (
      eventElement &&
      visualElement.contains(eventElement) &&
      isPointInsideRect(x, y, rect)
    ) {
      return false;
    }

    addHorizontalHit(hits, pos, node.nodeSize, x, y, rect);
    addVerticalHit(hits, pos, node.nodeSize, x, y, rect);
    return false;
  });

  return hits.sort((left, right) => left.distance - right.distance)[0] ?? null;
}

function isPointInsideRect(x: number, y: number, rect: DOMRect): boolean {
  const insideHorizontally = x >= rect.left && x <= rect.right;
  if (!insideHorizontally) {
    return false;
  }
  return y >= rect.top && y <= rect.bottom;
}

function isGapCursorClickArea(
  view: EditorView,
  eventTarget: EventTarget | null,
  eventElement: Element | null,
  nodeElement: HTMLElement
): boolean {
  if (eventTarget === view.dom) {
    return true;
  }
  if (!eventElement) {
    return false;
  }

  if (
    eventElement === nodeElement ||
    nodeElement.contains(eventElement) ||
    eventElement.contains(nodeElement)
  ) {
    return true;
  }

  const clickArea = eventElement.closest<HTMLElement>(
    "[data-gap-cursor-click-area]"
  );
  if (!clickArea) {
    return false;
  }
  return nodeElement.contains(clickArea);
}

function addBeforeCornerHit(
  hits: GapCursorMouseHit[],
  pos: number,
  x: number,
  y: number,
  rect: DOMRect
) {
  if (!isPointInsideBeforeCorner(x, y, rect)) {
    return;
  }

  hits.push({
    pos,
    side: "before",
    distance: Math.hypot(x - rect.left, y - rect.top),
    source: "corner",
  });
}

function isPointInsideBeforeCorner(x: number, y: number, rect: DOMRect) {
  return (
    x >= rect.left &&
    x <= rect.left + GAP_CURSOR_CORNER_CLICK_DISTANCE &&
    y >= rect.top &&
    y <= rect.top + GAP_CURSOR_CORNER_CLICK_DISTANCE
  );
}

function addHorizontalHit(
  hits: GapCursorMouseHit[],
  pos: number,
  nodeSize: number,
  x: number,
  y: number,
  rect: DOMRect
) {
  const verticallyAligned = y >= rect.top && y <= rect.bottom;
  if (!verticallyAligned) {
    return;
  }

  if (x < rect.left) {
    hits.push({
      pos,
      side: "before",
      distance: rect.left - x,
      source: "gutter",
    });
    return;
  }
  if (x > rect.right) {
    hits.push({
      pos: pos + nodeSize,
      side: "after",
      distance: x - rect.right,
      source: "gutter",
    });
  }
}

function addVerticalHit(
  hits: GapCursorMouseHit[],
  pos: number,
  nodeSize: number,
  x: number,
  y: number,
  rect: DOMRect
) {
  const horizontallyAligned = x >= rect.left && x <= rect.right;
  if (!horizontallyAligned) {
    return;
  }

  const distanceAbove = rect.top - y;
  if (distanceAbove > 0 && distanceAbove <= VERTICAL_GAP_CLICK_DISTANCE) {
    hits.push({
      pos,
      side: "before",
      distance: distanceAbove,
      source: "gutter",
    });
    return;
  }

  const distanceBelow = y - rect.bottom;
  if (distanceBelow > 0 && distanceBelow <= VERTICAL_GAP_CLICK_DISTANCE) {
    hits.push({
      pos: pos + nodeSize,
      side: "after",
      distance: distanceBelow,
      source: "gutter",
    });
  }
}

function getGapCursorVisualElement(element: HTMLElement) {
  return (
    element.querySelector<HTMLElement>("[data-gap-cursor-anchor]") ?? element
  );
}

function positionCursor(
  cursor: HTMLElement,
  target: HTMLElement,
  side: GapCursorSide
) {
  cursor.style.transform = "";
  const cursorRect = cursor.getBoundingClientRect();
  const targetRect = target.getBoundingClientRect();
  const targetX = side === "before" ? targetRect.left : targetRect.right;
  const targetY = side === "before" ? targetRect.top : targetRect.bottom;
  const offsetX = targetX - cursorRect.left;
  const offsetY = targetY - cursorRect.top;
  cursor.style.transform = `translate(${offsetX}px, ${offsetY}px)`;
}
