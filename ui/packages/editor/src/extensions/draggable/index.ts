import { Editor, Extension } from "@/tiptap/vue-3";
import {
  Fragment,
  Node,
  NodeType,
  ResolvedPos,
  Slice,
  NodeSelection,
  Plugin,
  PluginKey,
  Decoration,
  DecorationSet,
  // @ts-ignore
  __serializeForClipboard as serializeForClipboard,
} from "@/tiptap/pm";
import {} from "@/tiptap";
import type { EditorView } from "@/tiptap/pm";
import type { DraggableItem, ExtensionOptions } from "@/types";

// https://developer.mozilla.org/zh-CN/docs/Web/API/HTML_Drag_and_Drop_API
// https://github.com/ueberdosis/tiptap/blob/7832b96afbfc58574785043259230801e179310f/demos/src/Experiments/GlobalDragHandle/Vue/DragHandle.js
export interface ActiveNode {
  $pos: ResolvedPos;
  node: Node;
  el: HTMLElement;
  offset: number;
  domOffsetLeft: number;
  domOffsetTop: number;
}

let draggableItem: DraggableItem | boolean | undefined = undefined;
let draggableHandleDom: HTMLElement | null = null;
let currEditorView: EditorView;
let activeNode: ActiveNode | null = null;
let activeSelection: NodeSelection | null = null;
let mouseleaveTimer: any;
let dragging = false;
let hoverOrClickDragItem = false;

const createDragHandleDom = () => {
  const dom = document.createElement("div");
  dom.classList.add("draggable");
  dom.draggable = true;
  dom.setAttribute("data-drag-handle", "true");
  return dom;
};

const showDragHandleDOM = () => {
  draggableHandleDom?.classList?.add("show");
  draggableHandleDom?.classList?.remove("hide");
};

const hideDragHandleDOM = () => {
  draggableHandleDom?.classList?.remove("show");
  draggableHandleDom?.classList?.remove("active");
  draggableHandleDom?.classList?.add("hide");
};

/**
 * Correct the position of draggableHandleDom to match the current position of activeNode.
 *
 * @param view
 * @param referenceRectDOM
 */
const renderDragHandleDOM = (
  view: EditorView,
  activeNode: ActiveNode | undefined
) => {
  const root = view.dom.parentElement;
  if (!root) {
    return;
  }

  if (!draggableHandleDom) {
    return;
  }

  const referenceRectDOM = activeNode?.el;
  if (!referenceRectDOM) {
    return;
  }

  const targetNodeRect = referenceRectDOM.getBoundingClientRect();
  const rootRect = root.getBoundingClientRect();
  const handleRect = draggableHandleDom.getBoundingClientRect();

  const left =
    targetNodeRect.left -
    rootRect.left -
    handleRect.width -
    5 +
    activeNode.domOffsetLeft;
  const top =
    targetNodeRect.top -
    rootRect.top +
    handleRect.height / 2 +
    root.scrollTop +
    activeNode.domOffsetTop;
  draggableHandleDom.style.left = `${left}px`;
  draggableHandleDom.style.top = `${top - 2}px`;

  showDragHandleDOM();
};

const handleMouseEnterEvent = () => {
  if (!activeNode) {
    return;
  }
  hoverOrClickDragItem = true;
  currEditorView.dispatch(currEditorView.state.tr);
  clearTimeout(mouseleaveTimer);
  showDragHandleDOM();
};

const handleMouseLeaveEvent = () => {
  if (!activeNode) {
    return;
  }
  hoverOrClickDragItem = false;
  currEditorView.dispatch(currEditorView.state.tr);
  hideDragHandleDOM();
};

const handleMouseDownEvent = () => {
  if (!activeNode) {
    return null;
  }
  hoverOrClickDragItem = false;
  currEditorView.dispatch(currEditorView.state.tr);
  if (NodeSelection.isSelectable(activeNode.node)) {
    const nodeSelection = NodeSelection.create(
      currEditorView.state.doc,
      activeNode.$pos.pos - activeNode.offset
    );
    currEditorView.dispatch(
      currEditorView.state.tr.setSelection(nodeSelection)
    );
    currEditorView.focus();
    activeSelection = nodeSelection;
    return nodeSelection;
  }
  return null;
};

const handleMouseUpEvent = () => {
  if (!dragging) return;

  dragging = false;
  activeSelection = null;
  activeNode = null;
};

const handleDragStartEvent = (event: DragEvent) => {
  dragging = true;
  hoverOrClickDragItem = false;
  if (event.dataTransfer && activeNode && activeSelection) {
    const slice = activeSelection.content();
    event.dataTransfer.effectAllowed = "move";

    const { dom, text } = serializeForClipboard(currEditorView, slice);
    event.dataTransfer.clearData();
    event.dataTransfer.setData("text/html", dom.innerHTML);
    event.dataTransfer.setData("text/plain", text);
    event.dataTransfer.setDragImage(activeNode?.el as any, 0, 0);

    currEditorView.dragging = {
      slice,
      move: true,
    };
  }
};

const getDOMByPos = (
  view: EditorView,
  root: HTMLElement,
  $pos: ResolvedPos
) => {
  const { node } = view.domAtPos($pos.pos);

  let el: HTMLElement = node as HTMLElement;
  let parent = el.parentElement;
  while (parent && parent !== root && $pos.pos === view.posAtDOM(parent, 0)) {
    el = parent;
    parent = parent.parentElement;
  }

  return el;
};

const getPosByDOM = (
  view: EditorView,
  dom: HTMLElement
): ResolvedPos | null => {
  const domPos = view.posAtDOM(dom, 0);
  if (domPos < 0) {
    return null;
  }
  return view.state.doc.resolve(domPos);
};

export const selectAncestorNodeByDom = (
  dom: HTMLElement,
  view: EditorView
): ActiveNode | null => {
  const root = view.dom.parentElement;
  if (!root) {
    return null;
  }
  const $pos = getPosByDOM(view, dom);
  if (!$pos) {
    return null;
  }
  const node = $pos.node();
  const el = getDOMByPos(view, root, $pos);
  return { node, $pos, el, offset: 1, domOffsetLeft: 0, domOffsetTop: 0 };
};

const getExtensionDraggableItem = (editor: Editor, node: Node) => {
  const extension = editor.extensionManager.extensions.find((extension) => {
    return extension.name === node.type.name;
  });
  if (!extension) {
    return;
  }
  const draggableItem = (extension.options as ExtensionOptions).getDraggable?.({
    editor,
  });
  return draggableItem;
};

/**
 * According to the extension, obtain different rendering positions.
 *
 * @param editor
 * @param parentNode
 * @param dom
 * @returns
 **/
const getRenderContainer = (
  view: EditorView,
  draggableItem: DraggableItem | undefined,
  dom: HTMLElement
): ActiveNode => {
  const renderContainer = draggableItem?.getRenderContainer?.({ dom, view });
  const node = selectAncestorNodeByDom(renderContainer?.el || dom, view);
  return {
    el: renderContainer?.el || dom,
    node: renderContainer?.node || (node?.node as Node),
    $pos: renderContainer?.$pos || (node?.$pos as ResolvedPos),
    offset: renderContainer?.nodeOffset || (node?.offset as number),
    domOffsetLeft: renderContainer?.dragDomOffset?.x || 0,
    domOffsetTop: renderContainer?.dragDomOffset?.y || 0,
  };
};

const findParentNodeByDepth = (
  view: EditorView,
  dom: HTMLElement,
  depth = 1
): Node | undefined => {
  const $pos = getPosByDOM(view, dom);
  if (!$pos) {
    return;
  }
  if (depth > $pos.depth) {
    // 解决 audio 等不是块元素的问题，目前仅寻找其直接第一个子节点
    if (depth - $pos.depth == 1) {
      const parentNode = $pos.node();
      if (parentNode.firstChild && !parentNode.firstChild.type.isBlock) {
        return parentNode.firstChild;
      }
    }
    return;
  }
  const node = $pos.node(depth);
  if (!node) {
    return;
  }
  return node;
};

const getDraggableItem = ({
  editor,
  view,
  dom,
  event,
  depth = 1,
}: {
  editor: Editor;
  view: EditorView;
  dom: HTMLElement;
  event?: any;
  depth?: number;
}): DraggableItem | boolean | undefined => {
  const parentNode = findParentNodeByDepth(view, dom, depth);
  if (!parentNode) {
    return;
  }
  const draggableItem = getExtensionDraggableItem(editor, parentNode);
  if (draggableItem) {
    if (typeof draggableItem === "boolean") {
      return draggableItem;
    }
    const container = getRenderContainer(view, draggableItem, dom);
    const coords = { left: event.clientX, top: event.clientY };
    const pos = view.posAtCoords(coords);

    if (pos) {
      if (pos.inside == -1) {
        return draggableItem;
      }

      if (
        !(
          pos.inside >= container.$pos.start() &&
          pos.inside <= container.$pos.end()
        )
      ) {
        return draggableItem;
      }
    }

    if (draggableItem.allowPropagationDownward) {
      const draggable = getDraggableItem({
        editor,
        view,
        dom,
        event,
        depth: ++depth,
      });

      if (draggable) {
        return draggable;
      }
    }

    return draggableItem;
  }

  return getDraggableItem({
    editor,
    view,
    dom,
    event,
    depth: ++depth,
  });
};

/**
 * Get the insertion point of the target position relative to doc
 *
 * @param doc
 * @param pos
 * @param slice
 * @returns
 */
const dropPoint = (doc: Node, pos: number, slice: Slice) => {
  const $pos = doc.resolve(pos);
  if (!slice.content.size) {
    return pos;
  }
  let content: Fragment | undefined = slice.content;
  for (let i = 0; i < slice.openStart; i++) {
    content = content?.firstChild?.content;
  }
  for (
    let pass = 1;
    pass <= (slice.openStart == 0 && slice.size ? 2 : 1);
    pass++
  ) {
    for (let dep = $pos.depth; dep >= 0; dep--) {
      const bias =
        dep == $pos.depth
          ? 0
          : $pos.pos <= ($pos.start(dep + 1) + $pos.end(dep + 1)) / 2
          ? -1
          : 1;
      const insertPos = $pos.index(dep) + (bias > 0 ? 1 : 0);
      const parent = $pos.node(dep);
      let fits = false;
      if (pass == 1) {
        fits = parent.canReplace(insertPos, insertPos, content);
      } else {
        const wrapping = parent
          .contentMatchAt(insertPos)
          .findWrapping(content?.firstChild?.type as NodeType);
        fits =
          (wrapping &&
            parent.canReplaceWith(insertPos, insertPos, wrapping[0])) ||
          false;
      }
      if (fits) {
        return bias == 0
          ? $pos.pos
          : bias < 0
          ? $pos.before(dep + 1)
          : $pos.after(dep + 1);
      }
    }
  }
  return null;
};

const Draggable = Extension.create({
  name: "draggable",
  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("node-draggable"),
        view: (view) => {
          draggableHandleDom = createDragHandleDom();
          draggableHandleDom.addEventListener(
            "mouseenter",
            handleMouseEnterEvent
          );
          draggableHandleDom.addEventListener(
            "mouseleave",
            handleMouseLeaveEvent
          );
          draggableHandleDom.addEventListener(
            "mousedown",
            handleMouseDownEvent
          );
          draggableHandleDom.addEventListener("mouseup", handleMouseUpEvent);
          draggableHandleDom.addEventListener(
            "dragstart",
            handleDragStartEvent
          );
          const viewDomParentNode = view.dom.parentNode as HTMLElement;
          viewDomParentNode.appendChild(draggableHandleDom);
          viewDomParentNode.style.position = "relative";
          return {
            update: (view) => {
              currEditorView = view;
            },
            destroy: () => {
              if (!draggableHandleDom) {
                return;
              }
              clearTimeout(mouseleaveTimer);
              draggableHandleDom.removeEventListener(
                "mouseenter",
                handleMouseEnterEvent
              );
              draggableHandleDom.removeEventListener(
                "mouseleave",
                handleMouseLeaveEvent
              );
              draggableHandleDom.removeEventListener(
                "mousedown",
                handleMouseDownEvent
              );
              draggableHandleDom.removeEventListener(
                "mouseup",
                handleMouseUpEvent
              );
              draggableHandleDom.removeEventListener(
                "dragstart",
                handleDragStartEvent
              );
              draggableHandleDom.remove();
            },
          };
        },
        props: {
          handleDOMEvents: {
            // @ts-ignore
            mousemove: (view: EditorView, event) => {
              const coords = { left: event.clientX, top: event.clientY };
              const pos = view.posAtCoords(coords);
              if (!pos || !pos.pos) return false;

              const nodePos = pos.inside > -1 ? pos.inside : pos.pos;

              const nodeDom =
                view.nodeDOM(nodePos) ||
                view.domAtPos(nodePos)?.node ||
                event.target;
              if (!nodeDom) {
                hideDragHandleDOM();
                return false;
              }

              let dom: HTMLElement | null = nodeDom as HTMLElement;
              while (dom && dom.nodeType === 3) {
                dom = dom.parentElement;
              }
              if (!(dom instanceof HTMLElement)) {
                hideDragHandleDOM();
                return false;
              }
              const editor = this.editor;
              draggableItem = getDraggableItem({
                // @ts-ignore
                editor,
                view,
                dom,
                event,
              });
              // skip the current extension if getDraggable() is not implemented or returns false.
              if (!draggableItem) {
                return false;
              }
              if (typeof draggableItem === "boolean") {
                activeNode = selectAncestorNodeByDom(dom, view);
              } else {
                activeNode = getRenderContainer(view, draggableItem, dom);
              }
              if (!activeNode) {
                return;
              }
              renderDragHandleDOM(view, activeNode);
              return false;
            },
            mouseleave: () => {
              clearTimeout(mouseleaveTimer);
              mouseleaveTimer = setTimeout(() => {
                hideDragHandleDOM();
              }, 400);
              return false;
            },
          },
          handleKeyDown() {
            if (!draggableHandleDom) return false;
            draggableItem = undefined;
            hideDragHandleDOM();
            return false;
          },
          handleDrop: (view, event, slice) => {
            if (!draggableHandleDom) {
              return false;
            }
            if (!activeSelection) {
              return false;
            }
            const eventPos = view.posAtCoords({
              left: event.clientX,
              top: event.clientY,
            });
            if (!eventPos) {
              return true;
            }

            const $mouse = view.state.doc.resolve(eventPos.pos);
            const insertPos = dropPoint(view.state.doc, $mouse.pos, slice);
            if (!insertPos) {
              return false;
            }

            let isDisableDrop = false;
            if (dragging) {
              if (typeof draggableItem !== "boolean") {
                const handleDrop = draggableItem?.handleDrop?.({
                  view,
                  event,
                  slice,
                  insertPos,
                  node: activeNode?.node as Node,
                  selection: activeSelection,
                });
                if (typeof handleDrop === "boolean") {
                  isDisableDrop = handleDrop;
                }
              }
            }
            dragging = false;
            draggableItem = undefined;
            activeSelection = null;
            activeNode = null;
            return isDisableDrop;
          },
          decorations: (state) => {
            if (!hoverOrClickDragItem || !activeNode) {
              return DecorationSet.empty;
            }
            const { $pos } = activeNode;
            return DecorationSet.create(state.doc, [
              Decoration.node($pos.before(), $pos.after(), {
                class: "has-draggable-handle",
              }),
            ]);
          },
        },
      }),
    ];
  },
});

export default Draggable;
