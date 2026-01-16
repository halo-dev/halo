import { EditorView, Extension } from "@/tiptap";
import { Plugin, PluginKey } from "@tiptap/pm/state";

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
 * Smart scroll extension
 *
 * When the cursor is close to the top or bottom of the viewport, trigger scrolling to keep the cursor in the center of the viewport
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
    const options = this.options;
    let lastMouseDownTime = 0;

    return [
      new Plugin({
        key: new PluginKey("smartScroll"),
        view() {
          return {
            update(view, prevState) {
              if (!prevState) {
                return;
              }

              const { state } = view;
              const { doc, selection } = state;

              const docChanged = prevState.doc !== doc;
              const selectionChanged = !prevState.selection.eq(selection);

              const isRecentMouseClick = Date.now() - lastMouseDownTime < 100;

              // The conditions for triggering scrolling:
              // 1. Document content changed (input, delete, etc.)
              // 2. Selection changed but not caused by mouse click (arrow keys, etc.)
              if (docChanged || (selectionChanged && !isRecentMouseClick)) {
                requestAnimationFrame(() => {
                  smartScroll(view, options);
                });
              }
            },
          };
        },
        props: {
          handleDOMEvents: {
            mousedown: () => {
              lastMouseDownTime = Date.now();
              return false;
            },
          },
        },
      }),
    ];
  },
});

const smartScroll = (view: EditorView, options: SmartScrollOptions): void => {
  try {
    const { state } = view;
    const { selection } = state;

    const coords = view.coordsAtPos(selection.$head.pos);
    if (!coords) {
      return;
    }

    let scrollContainer: HTMLElement | null = null;
    if (!options.scrollContainer) {
      const editorElement = view.dom as HTMLElement;
      scrollContainer = findScrollContainer(editorElement);
    } else {
      if (typeof options.scrollContainer === "function") {
        scrollContainer = options.scrollContainer(view);
      } else if (typeof options.scrollContainer === "string") {
        scrollContainer = document.querySelector(
          options.scrollContainer
        ) as HTMLElement;
      } else {
        scrollContainer = options.scrollContainer;
      }
    }

    if (!scrollContainer) {
      return;
    }

    const containerRect = scrollContainer.getBoundingClientRect();
    const viewportTop = containerRect.top;
    const viewportBottom = containerRect.bottom;

    const cursorTop = coords.top;
    const cursorBottom = coords.bottom;

    const distanceFromTop = cursorTop - viewportTop;
    const distanceFromBottom = viewportBottom - cursorBottom;

    let scrollAmount = 0;

    if (distanceFromTop < options.topThreshold && distanceFromTop > 0) {
      scrollAmount = distanceFromTop - options.topThreshold;
    } else if (
      distanceFromBottom < options.bottomThreshold &&
      distanceFromBottom > 0
    ) {
      scrollAmount = options.bottomThreshold - distanceFromBottom;
    } else if (distanceFromTop < 0) {
      scrollAmount = distanceFromTop - options.topThreshold;
    } else if (distanceFromBottom < 0) {
      scrollAmount = options.bottomThreshold - distanceFromBottom;
    }

    if (scrollAmount !== 0) {
      const currentScrollTop = scrollContainer.scrollTop;
      const targetScrollTop = currentScrollTop + scrollAmount;

      if (scrollContainer.scrollTo) {
        scrollContainer.scrollTo({
          top: targetScrollTop,
          behavior: options.smooth ? "smooth" : "instant",
        });
      } else {
        scrollContainer.scrollTop = targetScrollTop;
      }
    }
  } catch (error) {
    console.debug("Smart scroll error:", error);
  }
};

/**
 * Find the scrollable container element
 */
const findScrollContainer = (element: HTMLElement): HTMLElement | null => {
  let current: HTMLElement | null = element;

  while (current && current !== document.body) {
    const style = window.getComputedStyle(current);
    const overflowY = style.overflowY;

    if (
      (overflowY === "auto" || overflowY === "scroll") &&
      current.scrollHeight > current.clientHeight
    ) {
      return current;
    }

    current = current.parentElement;
  }

  return document.documentElement;
};
