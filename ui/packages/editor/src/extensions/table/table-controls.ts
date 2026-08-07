import { Extension, type Editor } from "@tiptap/core";
import { i18n } from "@/locales";
import {
  addColumnAfter,
  addRowAfter,
  Decoration,
  DecorationSet,
  Plugin,
  PluginKey,
  TableMap,
  type EditorView,
} from "@/tiptap/pm";
import {
  findTable,
  isColumnSelected,
  isRowSelected,
  isTableSelected,
  selectRow,
} from "@/utils/table";
import { MIN_ROW_HEIGHT, normalizeRowHeight } from "./attributes";

export const TABLE_CONTROLS_PLUGIN_KEY = new PluginKey("haloTableControls");

type TableControl =
  | "table"
  | "row"
  | "column"
  | "add-row"
  | "add-column"
  | "add-row-before"
  | "add-column-before"
  | "resize-row";

interface DragState {
  axis: "row" | "column";
  index: number;
}

export const TableControls = Extension.create({
  name: "haloTableControls",

  addProseMirrorPlugins() {
    const editor = this.editor;
    let activeResizeCleanup: (() => void) | undefined;
    let dragState: DragState | undefined;
    let focusFrame: number | undefined;
    let layoutFrame: number | undefined;
    let observedWrapper: HTMLElement | undefined;
    let resizeObserver: ResizeObserver | undefined;

    return [
      new Plugin({
        key: TABLE_CONTROLS_PLUGIN_KEY,
        props: {
          decorations(state) {
            const table = findTable(state.selection);
            if (!table) {
              return DecorationSet.empty;
            }

            const decorations: Decoration[] = [];
            const map = TableMap.get(table.node);
            const columnAnchors = Array.from(
              { length: map.width },
              (_, index) => table.start + map.map[index]
            );
            const rowAnchors = Array.from(
              { length: map.height },
              (_, index) => table.start + map.map[index * map.width]
            );

            columnAnchors.forEach((pos, index) => {
              const selected = isColumnSelected(index)(state.selection);
              if (index === 0) {
                decorations.push(
                  Decoration.widget(
                    pos + 1,
                    () =>
                      createControl({
                        control: "add-column-before",
                        index,
                        label: i18n.global.t(
                          "editor.menus.table.add_column_before"
                        ),
                        className: [
                          "table-add-control",
                          "table-add-column",
                          "table-add-leading",
                        ],
                      }),
                    {
                      key: `table-add-column-before-${table.pos}`,
                      side: -1,
                    }
                  )
                );
              }
              decorations.push(
                Decoration.widget(
                  pos + 1,
                  () =>
                    createControl({
                      control: "column",
                      index,
                      label: i18n.global.t("editor.menus.table.select_column", {
                        index: index + 1,
                      }),
                      className: [
                        "grip-column",
                        index === 0 ? "first" : "",
                        index === columnAnchors.length - 1 ? "last" : "",
                        selected ? "selected" : "",
                      ],
                      draggable: true,
                    }),
                  {
                    key: `table-column-${table.pos}-${index}-${selected}`,
                    side: -1,
                  }
                ),
                Decoration.widget(
                  pos + 1,
                  () =>
                    createControl({
                      control: "add-column",
                      index,
                      label: i18n.global.t(
                        "editor.menus.table.add_column_after"
                      ),
                      className: ["table-add-control", "table-add-column"],
                    }),
                  { key: `table-add-column-${table.pos}-${index}`, side: 1 }
                )
              );
            });

            rowAnchors.forEach((pos, index) => {
              const selected = isRowSelected(index)(state.selection);
              if (index === 0) {
                decorations.push(
                  Decoration.widget(
                    pos + 1,
                    () =>
                      createControl({
                        control: "add-row-before",
                        index,
                        label: i18n.global.t(
                          "editor.menus.table.add_row_before"
                        ),
                        className: [
                          "table-add-control",
                          "table-add-row",
                          "table-add-leading",
                        ],
                      }),
                    {
                      key: `table-add-row-before-${table.pos}`,
                      side: -1,
                    }
                  )
                );
              }
              decorations.push(
                Decoration.widget(
                  pos + 1,
                  () =>
                    createControl({
                      control: "row",
                      index,
                      label: i18n.global.t("editor.menus.table.select_row", {
                        index: index + 1,
                      }),
                      className: [
                        "grip-row",
                        index === 0 ? "first" : "",
                        index === rowAnchors.length - 1 ? "last" : "",
                        selected ? "selected" : "",
                      ],
                      draggable: true,
                    }),
                  {
                    key: `table-row-${table.pos}-${index}-${selected}`,
                    side: -1,
                  }
                ),
                Decoration.widget(
                  pos + 1,
                  () =>
                    createControl({
                      control: "add-row",
                      index,
                      label: i18n.global.t("editor.menus.table.add_row_after"),
                      className: ["table-add-control", "table-add-row"],
                    }),
                  { key: `table-add-row-${table.pos}-${index}`, side: 1 }
                ),
                Decoration.widget(
                  pos + 1,
                  () =>
                    createControl({
                      control: "resize-row",
                      index,
                      label: i18n.global.t("editor.menus.table.resize_row", {
                        index: index + 1,
                      }),
                      className: ["row-resize-handle"],
                    }),
                  { key: `table-resize-row-${table.pos}-${index}`, side: 1 }
                )
              );
            });

            if (columnAnchors[0] !== undefined) {
              const selected = isTableSelected(state.selection);
              decorations.push(
                Decoration.widget(
                  columnAnchors[0] + 1,
                  () =>
                    createControl({
                      control: "table",
                      index: 0,
                      label: i18n.global.t("editor.menus.table.select_table"),
                      className: ["grip-table", selected ? "selected" : ""],
                    }),
                  {
                    key: `table-grip-${table.pos}-${selected}`,
                    side: -1,
                  }
                )
              );
            }

            return DecorationSet.create(state.doc, decorations);
          },

          handleDOMEvents: {
            mousedown(view, event) {
              const control = getControl(event.target);
              if (!control) {
                return false;
              }

              event.preventDefault();
              event.stopPropagation();
              const index = getControlIndex(control);

              if (control.dataset.tableControl === "resize-row") {
                activeResizeCleanup?.();
                activeResizeCleanup = startRowResize(
                  event,
                  index,
                  view,
                  (height) => {
                    editor.chain().focus().setTableRowHeight(height).run();
                  }
                );
                return true;
              }

              activateControl(view, editor, control);
              view.focus();
              return true;
            },

            keydown(view, event) {
              const control = getControl(event.target);
              if (
                !control ||
                (event.key !== "Enter" && event.key !== " ") ||
                control.dataset.tableControl === "resize-row"
              ) {
                return false;
              }

              event.preventDefault();
              event.stopPropagation();
              const type = control.dataset.tableControl;
              const index = control.dataset.tableIndex;
              activateControl(view, editor, control);
              if (focusFrame !== undefined) {
                cancelAnimationFrame(focusFrame);
              }
              focusFrame = requestAnimationFrame(() => {
                focusFrame = undefined;
                view.dom
                  .querySelector<HTMLElement>(
                    `[data-table-control="${type}"][data-table-index="${index}"]`
                  )
                  ?.focus();
              });
              return true;
            },

            dragstart(_view, event) {
              const control = getControl(event.target);
              const type = control?.dataset.tableControl;
              if (!control || (type !== "row" && type !== "column")) {
                return false;
              }

              dragState = {
                axis: type,
                index: getControlIndex(control),
              };
              control.classList.add("dragging");
              event.dataTransfer?.setData(
                "application/x-halo-table-control",
                JSON.stringify(dragState)
              );
              if (event.dataTransfer) {
                event.dataTransfer.effectAllowed = "move";
              }
              return true;
            },

            dragover(view, event) {
              const control = getControl(event.target);
              if (
                dragState &&
                control?.dataset.tableControl === dragState.axis
              ) {
                event.preventDefault();
                clearDragTargetClasses(view, control);
                control.classList.add("drag-target");
                return true;
              }
              clearDragTargetClasses(view);
              return false;
            },

            dragleave(_view, event) {
              const control = getControl(event.target);
              control?.classList.remove("drag-target");
              return false;
            },

            dragend(view) {
              dragState = undefined;
              clearDragClasses(view);
              return false;
            },

            drop(view, event) {
              const control = getControl(event.target);
              if (
                !dragState ||
                control?.dataset.tableControl !== dragState.axis
              ) {
                return false;
              }

              event.preventDefault();
              const targetIndex = getControlIndex(control);
              if (targetIndex !== dragState.index) {
                const select =
                  dragState.axis === "row"
                    ? editor.commands.selectTableRow
                    : editor.commands.selectTableColumn;
                select(dragState.index);
                const command =
                  dragState.axis === "row"
                    ? editor.commands.moveTableRowTo
                    : editor.commands.moveTableColumnTo;
                command(targetIndex);
              }

              dragState = undefined;
              clearDragClasses(view);
              view.focus();
              return true;
            },
          },
        },

        view(view) {
          const scheduleLayout = () => {
            if (layoutFrame !== undefined) {
              cancelAnimationFrame(layoutFrame);
            }
            layoutFrame = requestAnimationFrame(() => {
              layoutFrame = undefined;
              positionTableControls(view);
              const wrapper =
                view.dom
                  .querySelector<HTMLElement>("[data-table-control]")
                  ?.closest<HTMLElement>(".halo-table-wrapper") ?? undefined;
              if (
                wrapper !== observedWrapper &&
                typeof ResizeObserver !== "undefined"
              ) {
                resizeObserver?.disconnect();
                observedWrapper = wrapper;
                if (wrapper) {
                  resizeObserver = new ResizeObserver(scheduleLayout);
                  resizeObserver.observe(wrapper);
                }
              }
            });
          };

          scheduleLayout();
          return {
            update() {
              scheduleLayout();
            },
            destroy() {
              activeResizeCleanup?.();
              activeResizeCleanup = undefined;
              dragState = undefined;
              resizeObserver?.disconnect();
              resizeObserver = undefined;
              observedWrapper = undefined;
              if (focusFrame !== undefined) {
                cancelAnimationFrame(focusFrame);
                focusFrame = undefined;
              }
              if (layoutFrame !== undefined) {
                cancelAnimationFrame(layoutFrame);
                layoutFrame = undefined;
              }
            },
          };
        },
      }),
    ];
  },
});

function createControl({
  control,
  index,
  label,
  className,
  draggable = false,
}: {
  control: TableControl;
  index: number;
  label: string;
  className: string[];
  draggable?: boolean;
}) {
  const element = document.createElement("button");
  element.type = "button";
  element.dataset.editorUi = "true";
  element.dataset.tableControl = control;
  element.dataset.tableIndex = String(index);
  element.className = className.filter(Boolean).join(" ");
  element.setAttribute("aria-label", label);
  if (control === "table" || control === "row" || control === "column") {
    element.setAttribute(
      "aria-pressed",
      String(className.includes("selected"))
    );
  }
  element.title = label;
  element.draggable = draggable;
  return element;
}

function activateControl(
  view: EditorView,
  editor: Editor,
  control: HTMLElement
) {
  const index = getControlIndex(control);
  switch (control.dataset.tableControl as TableControl) {
    case "table":
      editor.commands.selectCurrentTable();
      break;
    case "row":
      editor.commands.selectTableRow(index);
      break;
    case "column":
      editor.commands.selectTableColumn(index);
      break;
    case "add-row":
      editor.commands.selectTableRow(index);
      addRowAfter(view.state, view.dispatch);
      break;
    case "add-row-before":
      editor.commands.selectTableRow(index);
      editor.commands.addRowBefore();
      break;
    case "add-column":
      editor.commands.selectTableColumn(index);
      addColumnAfter(view.state, view.dispatch);
      break;
    case "add-column-before":
      editor.commands.selectTableColumn(index);
      editor.commands.addColumnBefore();
      break;
  }
}

function getControl(target: EventTarget | null) {
  return target instanceof Element
    ? target.closest<HTMLElement>("[data-table-control]")
    : null;
}

function getControlIndex(control: HTMLElement) {
  return Number.parseInt(control.dataset.tableIndex ?? "0", 10);
}

function startRowResize(
  event: MouseEvent,
  rowIndex: number,
  editorView: EditorView,
  commit: (height: number) => void
) {
  const row = (event.target as Element | null)?.closest("tr");
  if (!row) {
    return () => undefined;
  }
  editorView.dispatch(selectRow(rowIndex)(editorView.state.tr));

  const startY = event.clientY;
  const startHeight = row.getBoundingClientRect().height;
  let nextHeight = normalizeRowHeight(startHeight) ?? MIN_ROW_HEIGHT;
  editorView.dom.classList.add("table-row-resizing");

  const onMouseMove = (moveEvent: MouseEvent) => {
    nextHeight =
      normalizeRowHeight(startHeight + moveEvent.clientY - startY) ??
      MIN_ROW_HEIGHT;
    row.style.height = `${nextHeight}px`;
  };
  const cleanup = () => {
    editorView.dom.classList.remove("table-row-resizing");
    document.removeEventListener("mousemove", onMouseMove);
    document.removeEventListener("mouseup", onMouseUp);
  };
  const onMouseUp = () => {
    cleanup();
    commit(nextHeight);
  };

  document.addEventListener("mousemove", onMouseMove);
  document.addEventListener("mouseup", onMouseUp);
  return cleanup;
}

function clearDragClasses(view: EditorView) {
  view.dom
    .querySelectorAll(".dragging, .drag-target")
    .forEach((element) => element.classList.remove("dragging", "drag-target"));
}

function clearDragTargetClasses(view: EditorView, except?: HTMLElement) {
  view.dom.querySelectorAll<HTMLElement>(".drag-target").forEach((element) => {
    if (element !== except) {
      element.classList.remove("drag-target");
    }
  });
}

function positionTableControls(view: EditorView) {
  const control = view.dom.querySelector<HTMLElement>("[data-table-control]");
  const wrapper = control?.closest<HTMLElement>(".halo-table-wrapper");
  const table = wrapper?.querySelector<HTMLTableElement>("table");
  if (!table) {
    return;
  }

  const columns = Array.from(
    table.querySelectorAll<HTMLElement>("colgroup > col")
  );
  const rows = Array.from(table.querySelectorAll<HTMLTableRowElement>("tr"));

  wrapper
    ?.querySelectorAll<HTMLElement>(
      '[data-table-control="column"], [data-table-control="add-column"], [data-table-control="add-column-before"]'
    )
    .forEach((element) => {
      const cell = element.closest<HTMLTableCellElement>("td, th");
      const column = columns[getControlIndex(element)];
      if (!cell || !column) {
        return;
      }
      const cellRect = cell.getBoundingClientRect();
      const columnRect = column.getBoundingClientRect();
      element.style.right = "auto";
      if (element.dataset.tableControl === "column") {
        element.style.left = `${columnRect.left - cellRect.left}px`;
        element.style.width = `${columnRect.width}px`;
        return;
      }
      const boundary =
        element.dataset.tableControl === "add-column-before"
          ? columnRect.left
          : columnRect.right;
      const isLeading = element.dataset.tableControl === "add-column-before";
      const isTrailing =
        element.dataset.tableControl === "add-column" &&
        getControlIndex(element) === columns.length - 1;
      const offset = isLeading
        ? 0
        : isTrailing
          ? element.offsetWidth
          : element.offsetWidth / 2;
      element.style.left = `${boundary - cellRect.left - offset}px`;
    });

  wrapper
    ?.querySelectorAll<HTMLElement>(
      '[data-table-control="row"], [data-table-control="add-row"], [data-table-control="add-row-before"], [data-table-control="resize-row"]'
    )
    .forEach((element) => {
      const cell = element.closest<HTMLTableCellElement>("td, th");
      const row = rows[getControlIndex(element)];
      if (!cell || !row) {
        return;
      }
      const cellRect = cell.getBoundingClientRect();
      const rowRect = row.getBoundingClientRect();
      element.style.bottom = "auto";
      if (element.dataset.tableControl === "row") {
        element.style.top = `${rowRect.top - cellRect.top}px`;
        element.style.height = `${rowRect.height}px`;
        return;
      }
      const boundary =
        element.dataset.tableControl === "add-row-before"
          ? rowRect.top
          : rowRect.bottom;
      element.style.top = `${boundary - cellRect.top - element.offsetHeight / 2}px`;
    });
}
