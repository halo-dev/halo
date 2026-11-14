import { Plugin, PluginKey } from "@tiptap/pm/state";
import { EditorView, Extension } from "@/tiptap";
import { NodeSelection } from "@/tiptap/pm";
import {
  getCursorCoords,
  getNestedCursorCoords,
  type Coords,
} from "@/utils/get-cursor-coords";

export interface SmartScrollOptions {
  /**
   * The container to scroll
   */
  scrollContainer?:
    | string
    | HTMLElement
    | ((editor: EditorView) => HTMLElement);
  /**
   * Top threshold (pixels), when the cursor is less than this value from the top of the viewport, trigger scrolling
   * @default 150
   */
  topThreshold: number;

  /**
   * Bottom threshold (pixels), when the cursor is less than this value from the bottom of the viewport, trigger scrolling
   * @default 150
   */
  bottomThreshold: number;

  /**
   * Whether to enable smooth scrolling
   * @default false
   */
  smooth: boolean;
}

/**
 * Keeps explicit editor navigation inside a comfortable viewport buffer.
 *
 * ProseMirror handles the normal selection path. The plugin view only bridges
 * document synchronization from a verified nested contenteditable, whose
 * caret is not represented by the outer ProseMirror selection.
 */
export const ExtensionSmartScroll = Extension.create<SmartScrollOptions>({
  name: "smartScroll",

  addOptions() {
    return {
      scrollContainer: undefined,
      topThreshold: 150,
      bottomThreshold: 150,
      smooth: false,
    };
  },

  addProseMirrorPlugins() {
    const options = normalizeOptions(this.options);
    const useCustomSelectionScroll =
      Boolean(options.scrollContainer) || options.smooth;

    return [
      new Plugin({
        key: new PluginKey("smartScroll"),
        props: {
          scrollThreshold: {
            top: options.topThreshold,
            right: 0,
            bottom: options.bottomThreshold,
            left: 0,
          },
          scrollMargin: {
            top: options.topThreshold,
            right: 5,
            bottom: options.bottomThreshold,
            left: 5,
          },
          ...(useCustomSelectionScroll
            ? {
                handleScrollToSelection: (view: EditorView) =>
                  scrollSelectionIntoView(view, options),
              }
            : {}),
        },
        view() {
          let animationFrame: number | null = null;

          return {
            update(view, prevState) {
              if (prevState.doc.eq(view.state.doc)) {
                return;
              }
              if (!getNestedCursorCoords(view) || animationFrame !== null) {
                return;
              }

              animationFrame = requestAnimationFrame(() => {
                animationFrame = null;
                if (view.isDestroyed) {
                  return;
                }

                const nestedCursor = getNestedCursorCoords(view);
                if (!nestedCursor) {
                  return;
                }
                scrollCoordsIntoView(
                  view,
                  nestedCursor.coords,
                  nestedCursor.startDOM,
                  options
                );
              });
            },
            destroy() {
              if (animationFrame !== null) {
                cancelAnimationFrame(animationFrame);
                animationFrame = null;
              }
            },
          };
        },
      }),
    ];
  },
});

const normalizeOptions = (options: SmartScrollOptions): SmartScrollOptions => ({
  ...options,
  topThreshold: normalizeThreshold(options.topThreshold),
  bottomThreshold: normalizeThreshold(options.bottomThreshold),
});

const normalizeThreshold = (value: number): number =>
  Number.isFinite(value) ? Math.max(0, value) : 0;

const scrollSelectionIntoView = (
  view: EditorView,
  options: SmartScrollOptions
): boolean => {
  try {
    const nestedCursor = getNestedCursorCoords(view);
    if (nestedCursor) {
      return scrollCoordsIntoView(
        view,
        nestedCursor.coords,
        nestedCursor.startDOM,
        options
      );
    }

    const selection = view.state.selection;
    if (selection instanceof NodeSelection) {
      const nodeDOM = view.nodeDOM(selection.from);
      if (nodeDOM?.nodeType === Node.ELEMENT_NODE) {
        return scrollCoordsIntoView(
          view,
          (nodeDOM as Element).getBoundingClientRect(),
          nodeDOM,
          options
        );
      }
    }

    const coords = getCursorCoords(view);
    return coords
      ? scrollCoordsIntoView(view, coords, view.dom, options)
      : false;
  } catch {
    return false;
  }
};

const scrollCoordsIntoView = (
  view: EditorView,
  initialCoords: Coords,
  startDOM: Node,
  options: SmartScrollOptions
): boolean => {
  const containers = getScrollContainers(view, startDOM, options);
  if (containers.length === 0) {
    return false;
  }

  let coords = initialCoords;
  for (const container of containers) {
    const bounds = getVisibleBounds(container);
    const scrollAmount = getVerticalScrollAmount(coords, bounds, options);
    if (scrollAmount === 0) {
      continue;
    }

    const currentScrollTop = container.scrollTop;
    const maxScrollTop = Math.max(
      0,
      container.scrollHeight - container.clientHeight
    );
    const targetScrollTop = clamp(
      currentScrollTop + scrollAmount,
      0,
      maxScrollTop
    );
    const appliedAmount = targetScrollTop - currentScrollTop;
    if (appliedAmount === 0) {
      continue;
    }

    if (typeof container.scrollTo === "function") {
      container.scrollTo({
        top: targetScrollTop,
        behavior: options.smooth ? "smooth" : "instant",
      });
    } else {
      container.scrollTop = targetScrollTop;
    }

    // Keep geometry consistent while walking outer scroll ancestors. This is
    // also deterministic when a browser applies smooth scrolling later.
    coords = {
      top: coords.top - appliedAmount,
      bottom: coords.bottom - appliedAmount,
      left: coords.left,
      right: coords.right,
    };
  }

  return true;
};

const getVerticalScrollAmount = (
  coords: Coords,
  bounds: Pick<Coords, "top" | "bottom">,
  options: SmartScrollOptions
): number => {
  const viewportTop = bounds.top + options.topThreshold;
  const viewportBottom = bounds.bottom - options.bottomThreshold;

  if (coords.top < viewportTop) {
    return coords.top - viewportTop;
  }
  if (coords.bottom > viewportBottom) {
    const availableHeight = Math.max(0, viewportBottom - viewportTop);
    return coords.bottom - coords.top > availableHeight
      ? coords.top - viewportTop
      : coords.bottom - viewportBottom;
  }
  return 0;
};

const getScrollContainers = (
  view: EditorView,
  startDOM: Node,
  options: SmartScrollOptions
): HTMLElement[] => {
  if (options.scrollContainer) {
    const container = resolveScrollContainer(view, options.scrollContainer);
    return container && containsEditor(container, view.dom) ? [container] : [];
  }

  const containers: HTMLElement[] = [];
  const doc = view.dom.ownerDocument;
  const scrollingElement = (doc.scrollingElement ??
    doc.documentElement) as HTMLElement;
  let element =
    startDOM.nodeType === Node.ELEMENT_NODE
      ? (startDOM as HTMLElement)
      : startDOM.parentElement;

  while (element) {
    if (element === scrollingElement || isScrollable(element)) {
      containers.push(element);
    }

    const position = getComputedStyle(element).position;
    if (/^(fixed|sticky)$/.test(position)) {
      break;
    }
    element =
      position === "absolute"
        ? (element.offsetParent as HTMLElement | null)
        : element.parentElement;
  }

  if (!containers.includes(scrollingElement) && scrollingElement.isConnected) {
    containers.push(scrollingElement);
  }
  return containers;
};

const resolveScrollContainer = (
  view: EditorView,
  scrollContainer: NonNullable<SmartScrollOptions["scrollContainer"]>
): HTMLElement | null => {
  try {
    let container: HTMLElement | null;
    if (typeof scrollContainer === "function") {
      container = scrollContainer(view);
    } else if (typeof scrollContainer === "string") {
      container =
        view.dom.ownerDocument.querySelector<HTMLElement>(scrollContainer);
    } else {
      container = scrollContainer;
    }
    return container?.isConnected ? container : null;
  } catch {
    return null;
  }
};

const containsEditor = (container: HTMLElement, editor: HTMLElement) =>
  container === editor || container.contains(editor);

const isScrollable = (element: HTMLElement): boolean => {
  if (element.scrollHeight <= element.clientHeight) {
    return false;
  }
  return /^(auto|hidden|overlay|scroll)$/.test(
    getComputedStyle(element).overflowY
  );
};

const getVisibleBounds = (
  container: HTMLElement
): Pick<Coords, "top" | "bottom"> => {
  const doc = container.ownerDocument;
  if (container === doc.scrollingElement || container === doc.documentElement) {
    const viewport = doc.defaultView?.visualViewport;
    const top = viewport?.offsetTop ?? 0;
    const height = viewport?.height ?? doc.defaultView?.innerHeight ?? 0;
    return { top, bottom: top + height };
  }

  const rect = container.getBoundingClientRect();
  const top = rect.top + container.clientTop;
  return { top, bottom: top + container.clientHeight };
};

const clamp = (value: number, min: number, max: number): number =>
  Math.min(Math.max(value, min), max);
