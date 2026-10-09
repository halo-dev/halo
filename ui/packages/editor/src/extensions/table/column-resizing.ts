import {
  closeHistory,
  columnResizingPluginKey,
  Plugin,
  TableMap,
} from "@/tiptap/pm";
import { applyAutoColumnWidths, getTableColumnWidths } from "./column-widths";

export function autoColumnResizing(cellMinWidth: number) {
  let cleanupDrag: (() => void) | undefined;
  let updatePreview: (() => void) | undefined;

  return new Plugin({
    view: () => ({
      update: () => updatePreview?.(),
      destroy: () => cleanupDrag?.(),
    }),
    props: {
      handleDOMEvents: {
        mousedown(view, event) {
          const resizeState = columnResizingPluginKey.getState(view.state);
          if (
            !view.editable ||
            event.button !== 0 ||
            !resizeState ||
            resizeState.activeHandle < 0 ||
            resizeState.dragging
          ) {
            return false;
          }

          const $cell = view.state.doc.resolve(resizeState.activeHandle);
          const table = $cell.node(-1);
          if (table.attrs.layoutMode !== "auto") {
            return false;
          }

          const map = TableMap.get(table);
          const start = $cell.start(-1);
          const column =
            map.colCount($cell.pos - start) +
            $cell.nodeAfter!.attrs.colspan -
            1;
          // The outside edge cannot move while the table fills its container.
          event.preventDefault();
          if (column === map.width - 1) {
            return true;
          }

          const wrapper = view.nodeDOM(start - 1) as HTMLElement;
          const tableDOM = wrapper.querySelector("table")!;
          const colgroup = tableDOM.querySelector("colgroup")!;
          const widths = Array.from(
            colgroup.children,
            (col) => col.getBoundingClientRect().width
          );
          if (
            widths.length !== map.width ||
            widths.some((width) => width <= 0)
          ) {
            return true;
          }

          cleanupDrag?.();
          const originalDoc = view.state.doc;
          const startX = event.clientX;
          const pairWidth = widths[column] + widths[column + 1];
          const minimum = Math.min(
            cellMinWidth,
            widths[column],
            widths[column + 1]
          );
          const win = view.dom.ownerDocument.defaultView!;
          const resizedWidths = (clientX: number) => {
            const result = [...widths];
            result[column] = Math.max(
              minimum,
              Math.min(pairWidth - minimum, widths[column] + clientX - startX)
            );
            result[column + 1] = pairWidth - result[column];
            return result;
          };
          const cleanup = () => {
            win.removeEventListener("mousemove", move);
            win.removeEventListener("mouseup", finish);
            win.removeEventListener("blur", cancel);
            cleanupDrag = undefined;
            updatePreview = undefined;
          };
          const reset = () => {
            if (view.state.doc === originalDoc) {
              applyAutoColumnWidths(
                tableDOM,
                colgroup,
                getTableColumnWidths(table),
                cellMinWidth
              );
            }
            if (!view.isDestroyed) {
              view.dispatch(
                view.state.tr.setMeta(columnResizingPluginKey, {
                  setDragging: false,
                })
              );
            }
          };
          const cancel = () => {
            cleanup();
            reset();
          };
          const finish = (event: MouseEvent) => {
            cleanup();
            if (view.isDestroyed) {
              return;
            }
            if (view.state.doc !== originalDoc || event.clientX === startX) {
              reset();
              return;
            }
            const result = resizedWidths(event.clientX).map((width) =>
              Math.max(1, Math.round(width))
            );
            const tr = closeHistory(view.state.tr);
            table.descendants((node, pos) => {
              if (
                node.type.spec.tableRole !== "cell" &&
                node.type.spec.tableRole !== "header_cell"
              ) {
                return;
              }
              const rect = map.findCell(pos);
              tr.setNodeMarkup(start + pos, undefined, {
                ...node.attrs,
                colwidth: result.slice(rect.left, rect.right),
              });
              return false;
            });
            tr.setMeta(columnResizingPluginKey, { setDragging: false });
            view.dispatch(tr);
            view.dispatch(closeHistory(view.state.tr));
          };
          let previewWidths = widths;
          const renderPreview = () => {
            if (view.state.doc === originalDoc) {
              applyAutoColumnWidths(
                tableDOM,
                colgroup,
                previewWidths,
                cellMinWidth
              );
            }
          };
          const move = (event: MouseEvent) => {
            if (view.isDestroyed || view.state.doc !== originalDoc) {
              cancel();
            } else if (!event.buttons) {
              finish(event);
            } else {
              previewWidths = resizedWidths(event.clientX);
              renderPreview();
            }
          };

          view.dispatch(
            view.state.tr.setMeta(columnResizingPluginKey, {
              setDragging: { startX, startWidth: widths[column] },
            })
          );
          applyAutoColumnWidths(tableDOM, colgroup, widths, cellMinWidth);
          win.addEventListener("mousemove", move);
          win.addEventListener("mouseup", finish);
          win.addEventListener("blur", cancel);
          cleanupDrag = cleanup;
          updatePreview = renderPreview;
          return true;
        },
      },
    },
  });
}
