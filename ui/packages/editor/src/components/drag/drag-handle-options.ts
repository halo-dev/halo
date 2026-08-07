import type {
  DragHandleRule,
  NestedOptions,
} from "@tiptap/extension-drag-handle";

export const BLOCK_DRAG_HANDLE_OFFSET = 5;

/**
 * Preserve the visual gap while extending the handle's hit area across it.
 */
export const blockDragHandleBridgeStyle = {
  "--halo-drag-handle-hover-bridge": `${BLOCK_DRAG_HANDLE_OFFSET}px`,
};

export const blockDragHandleRule: DragHandleRule = {
  id: "topLevelOrListItemBlock",
  evaluate: ({ node, parent, depth, isFirst }) => {
    if (depth === 1) {
      return 0;
    }

    const isListItemChild = parent?.type.isInGroup("listItem");
    const isListWrapper = node.firstChild?.type.isInGroup("listItem") ?? false;
    if (isListItemChild && !isFirst && !isListWrapper) {
      return 0;
    }

    return 1000;
  },
};

/**
 * Use the official nested drag path for exact node boundaries. Top-level nodes
 * remain draggable as before, while standalone blocks inserted after a list
 * item's leading paragraph get their own handle. List rows and nested list
 * wrappers continue to be dragged through the surrounding top-level list.
 */
export const blockDragHandleOptions = {
  defaultRules: false,
  edgeDetection: "none",
  rules: [blockDragHandleRule],
} satisfies NestedOptions;
