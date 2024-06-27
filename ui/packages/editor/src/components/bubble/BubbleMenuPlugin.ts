import type { EditorView } from "@/tiptap/pm";
import { EditorState, Plugin, PluginKey } from "@/tiptap/pm";
import {
  Editor,
  isNodeSelection,
  isTextSelection,
  posToDOMRect,
} from "@/tiptap/vue-3";
import tippy, { sticky, type Instance, type Props } from "tippy.js";

export interface TippyOptionProps extends Props {
  fixed?: boolean;
}
export interface BubbleMenuPluginProps {
  pluginKey: PluginKey | string;
  editor: Editor;
  element: HTMLElement;
  tippyOptions?: Partial<TippyOptionProps>;
  updateDelay?: number;
  shouldShow?:
    | ((props: {
        editor: Editor;
        state: EditorState;
        node?: HTMLElement;
        view?: EditorView;
        oldState?: EditorState;
        from?: number;
        to?: number;
      }) => boolean)
    | null;
  getRenderContainer?: (node: HTMLElement) => HTMLElement;
  defaultAnimation?: boolean;
}

export type BubbleMenuViewProps = BubbleMenuPluginProps & {
  view: EditorView;
};

const ACTIVE_BUBBLE_MENUS: Instance[] = [];

export class BubbleMenuView {
  public editor: Editor;

  public element: HTMLElement;

  public view: EditorView;

  public preventHide = false;

  public tippy: Instance<TippyOptionProps> | undefined;

  public tippyOptions?: Partial<TippyOptionProps>;

  public getRenderContainer?: BubbleMenuPluginProps["getRenderContainer"];

  public defaultAnimation?: BubbleMenuPluginProps["defaultAnimation"];

  public shouldShow: Exclude<BubbleMenuPluginProps["shouldShow"], null> = ({
    view,
    state,
    from,
    to,
  }) => {
    const { doc, selection } = state as EditorState;
    const { empty } = selection;

    // Sometime check for `empty` is not enough.
    // Doubleclick an empty paragraph returns a node size of 2.
    // So we check also for an empty text size.
    const isEmptyTextBlock =
      !doc.textBetween(from || 0, to || 0).length && isTextSelection(selection);

    if (!(view as EditorView).hasFocus() || empty || isEmptyTextBlock) {
      return false;
    }

    return true;
  };

  constructor({
    editor,
    element,
    view,
    tippyOptions = {},
    shouldShow,
    getRenderContainer,
    defaultAnimation = true,
  }: BubbleMenuViewProps) {
    this.editor = editor;
    this.element = element;
    this.view = view;
    this.getRenderContainer = getRenderContainer;
    this.defaultAnimation = defaultAnimation;

    if (shouldShow) {
      this.shouldShow = shouldShow;
    }

    this.element.addEventListener("mousedown", this.mousedownHandler, {
      capture: true,
    });
    this.view.dom.addEventListener("dragstart", this.dragstartHandler);
    this.tippyOptions = tippyOptions || {};
    // Detaches menu content from its current parent
    this.element.remove();
    this.element.style.visibility = "visible";
  }

  mousedownHandler = () => {
    this.preventHide = true;
  };

  dragstartHandler = () => {
    this.hide();
  };

  blurHandler = ({ event }: { event: FocusEvent }) => {
    if (this.preventHide) {
      this.preventHide = false;

      return;
    }

    if (
      event?.relatedTarget &&
      this.element.parentNode?.contains(event.relatedTarget as Node)
    ) {
      return;
    }

    const shouldShow =
      this.editor.isEditable &&
      this.shouldShow?.({
        editor: this.editor,
        state: this.editor.state,
      });

    if (shouldShow) return;

    this.hide();
  };

  createTooltip() {
    const { element: editorElement } = this.editor.options;
    const editorIsAttached = !!editorElement.parentElement;

    if (this.tippy || !editorIsAttached) {
      return;
    }

    this.tippy = tippy(editorElement, {
      getReferenceClientRect: null,
      content: this.element,
      interactive: true,
      trigger: "manual",
      placement: "bottom-start",
      hideOnClick: "toggle",
      plugins: [sticky],
      popperOptions: {
        modifiers: [
          {
            name: "customWidth",
            enabled: true,
            phase: "beforeWrite",
            requires: ["computeStyles"],
            fn({ state }) {
              state.styles.popper.maxWidth = "98%";
            },
          },
        ],
      },
      ...Object.assign(
        {
          zIndex: 999,
          ...(this.defaultAnimation
            ? {
                animation: "shift-toward-subtle",
                moveTransition: "transform 0.2s ease-in-out",
              }
            : {}),
          fixed: true,
        },
        this.tippyOptions
      ),
    });

    // maybe we have to hide tippy on its own blur event as well
    if (this.tippy.popper.firstChild) {
      (this.tippy.popper.firstChild as HTMLElement).addEventListener(
        "blur",
        (event) => {
          this.blurHandler({ event });
        }
      );
    }
  }

  update(view: EditorView, oldState?: EditorState) {
    const { state, composing } = view;
    const { doc, selection } = state;
    const isSame =
      oldState && oldState.doc.eq(doc) && oldState.selection.eq(selection);

    if (composing || isSame) {
      return;
    }

    // support for CellSelections
    const { ranges } = selection;
    const from = Math.min(...ranges.map((range) => range.$from.pos));
    const to = Math.max(...ranges.map((range) => range.$to.pos));

    const domAtPos = view.domAtPos(from).node as HTMLElement;
    const nodeDOM = view.nodeDOM(from) as HTMLElement;
    const node = nodeDOM || domAtPos;
    const shouldShow =
      this.editor.isEditable &&
      this.shouldShow?.({
        editor: this.editor,
        view,
        node,
        state,
        oldState,
        from,
        to,
      });

    if (!shouldShow) {
      this.hide();
      return;
    }

    this.createTooltip();

    const cursorAt = selection.$anchor.pos;

    // prevent the menu from being obscured
    const placement = this.tippyOptions?.placement
      ? this.tippyOptions?.placement
      : isNodeSelection(selection)
      ? ACTIVE_BUBBLE_MENUS.length > 1
        ? "bottom"
        : "top"
      : this.tippy?.props.fixed
      ? "bottom-start"
      : Math.abs(cursorAt - to) <= Math.abs(cursorAt - from)
      ? "bottom-start"
      : "top-start";

    const otherBubbleMenus = ACTIVE_BUBBLE_MENUS.filter(
      (instance) =>
        instance.id !== this.tippy?.id &&
        instance.popperInstance &&
        instance.popperInstance.state
    );
    const offset = this.tippyOptions?.offset as [number, number];
    const offsetX = offset?.[0] ?? 0;
    let offsetY = otherBubbleMenus.length
      ? otherBubbleMenus.reduce((prev, instance, currentIndex, array) => {
          const prevY = array[currentIndex - 1]
            ? array[currentIndex - 1]?.popperInstance?.state?.modifiersData
                ?.popperOffsets?.y ?? 0
            : 0;
          const currentY =
            instance?.popperInstance?.state?.modifiersData?.popperOffsets?.y ??
            0;
          const currentHeight =
            instance?.popperInstance?.state?.rects?.popper?.height ?? 10;
          if (Math.abs(prevY - currentY) <= currentHeight) {
            prev += currentHeight;
          }

          return prev;
        }, 0)
      : offset?.[1] ?? 10;
    if (!offsetY) {
      offsetY = 10;
    }
    this.tippy?.setProps({
      offset: [offsetX, offsetY],
      placement,
      getReferenceClientRect: () => {
        let toMountNode = null;

        if (isNodeSelection(state.selection)) {
          if (this.getRenderContainer && node) {
            toMountNode = this.getRenderContainer(node);
          }
        }

        if (this.getRenderContainer && node) {
          toMountNode = this.getRenderContainer(node);
        }

        if (toMountNode && toMountNode.getBoundingClientRect) {
          return toMountNode.getBoundingClientRect();
        }

        if (node && node.getBoundingClientRect) {
          return node.getBoundingClientRect();
        }

        return posToDOMRect(view, from, to);
      },
    });

    this.show();
  }

  addActiveBubbleMenu = () => {
    const idx = ACTIVE_BUBBLE_MENUS.findIndex(
      (instance) => instance?.id === this.tippy?.id
    );
    if (idx < 0) {
      ACTIVE_BUBBLE_MENUS.push(this.tippy as Instance);
    }
  };

  removeActiveBubbleMenu = () => {
    const idx = ACTIVE_BUBBLE_MENUS.findIndex(
      (instance) => instance?.id === this.tippy?.id
    );
    if (idx > -1) {
      ACTIVE_BUBBLE_MENUS.splice(idx, 1);
    }
  };
  show() {
    this.addActiveBubbleMenu();
    this.tippy?.show();
  }

  hide() {
    this.removeActiveBubbleMenu();
    this.tippy?.hide();
  }

  destroy() {
    this.removeActiveBubbleMenu();
    this.tippy?.destroy();
    this.element.removeEventListener("mousedown", this.mousedownHandler, {
      capture: true,
    });
    this.view.dom.removeEventListener("dragstart", this.dragstartHandler);
    // this.editor.off("focus", this.focusHandler);
    // this.editor.off("blur", this.blurHandler);
  }
}

export const BubbleMenuPlugin = (options: BubbleMenuPluginProps) => {
  return new Plugin({
    key:
      typeof options.pluginKey === "string"
        ? new PluginKey(options.pluginKey)
        : options.pluginKey,
    view: (view) => new BubbleMenuView({ view, ...options }),
  });
};
