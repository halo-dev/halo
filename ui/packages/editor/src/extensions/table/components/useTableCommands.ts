import { computed, onBeforeUnmount, onMounted, shallowRef } from "vue";
import type { Editor } from "@/tiptap";
import { getTableHeaderState } from "@/utils/table";

export function useTableCommands(editor: Editor) {
  const revision = shallowRef(0);
  const fromEditorState = <T>(getValue: () => T) =>
    computed(() => {
      void revision.value;
      return getValue();
    });

  const update = () => {
    revision.value += 1;
  };

  onMounted(() => {
    editor.on("transaction", update);
  });

  onBeforeUnmount(() => {
    editor.off("transaction", update);
  });

  const layoutMode = fromEditorState(
    () =>
      (editor.getAttributes("table").layoutMode as "auto" | "fixed") ?? "auto"
  );

  const cellAttributes = fromEditorState(() => {
    const cell = editor.getAttributes("tableCell");
    if (Object.keys(cell).length > 0) {
      return cell;
    }
    return editor.getAttributes("tableHeader");
  });

  const horizontalAlign = computed(() =>
    ["left", "center", "right"].includes(cellAttributes.value.align)
      ? (cellAttributes.value.align as "left" | "center" | "right")
      : null
  );

  const verticalAlign = computed(() =>
    ["top", "middle", "bottom"].includes(cellAttributes.value.verticalAlign)
      ? (cellAttributes.value.verticalAlign as "top" | "middle" | "bottom")
      : null
  );

  const backgroundColor = computed(
    () => (cellAttributes.value.backgroundColor as string | null) ?? null
  );

  const rowHeight = fromEditorState(() => {
    const value = editor.getAttributes("tableRow").rowHeight;
    return typeof value === "number" ? value : null;
  });

  const headerState = fromEditorState(() =>
    getTableHeaderState(editor.state.selection)
  );

  const hasCellFormatting = computed(
    () =>
      horizontalAlign.value !== null ||
      verticalAlign.value !== null ||
      backgroundColor.value !== null
  );

  const can = {
    addRowBefore: fromEditorState(() => editor.can().addRowBefore()),
    addRowAfter: fromEditorState(() => editor.can().addRowAfter()),
    deleteRow: fromEditorState(() => editor.can().deleteRow()),
    addColumnBefore: fromEditorState(() => editor.can().addColumnBefore()),
    addColumnAfter: fromEditorState(() => editor.can().addColumnAfter()),
    deleteColumn: fromEditorState(() => editor.can().deleteColumn()),
    mergeCells: fromEditorState(() => editor.can().mergeCells()),
    splitCell: fromEditorState(() => editor.can().splitCell()),
    duplicateRow: fromEditorState(() => editor.can().duplicateTableRow()),
    duplicateColumn: fromEditorState(() => editor.can().duplicateTableColumn()),
    clearRow: fromEditorState(() => editor.can().clearSelectedTableRow()),
    clearColumn: fromEditorState(() => editor.can().clearSelectedTableColumn()),
    moveRowUp: fromEditorState(() => editor.can().moveTableRow(-1)),
    moveRowDown: fromEditorState(() => editor.can().moveTableRow(1)),
    moveColumnLeft: fromEditorState(() => editor.can().moveTableColumn(-1)),
    moveColumnRight: fromEditorState(() => editor.can().moveTableColumn(1)),
    toggleHeaderRow: fromEditorState(() => editor.can().toggleHeaderRow()),
    toggleHeaderColumn: fromEditorState(() =>
      editor.can().toggleHeaderColumn()
    ),
    clearFormatting: fromEditorState(() =>
      editor.can().clearTableCellFormatting()
    ),
  };

  return {
    layoutMode,
    horizontalAlign,
    verticalAlign,
    backgroundColor,
    rowHeight,
    isHeaderRow: computed(() => headerState.value.row),
    isHeaderColumn: computed(() => headerState.value.column),
    hasCellFormatting,
    can,
  };
}
