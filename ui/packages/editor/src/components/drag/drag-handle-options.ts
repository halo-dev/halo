import type {
  DragHandleRule,
  NestedOptions,
} from "@tiptap/extension-drag-handle";

export const topLevelDragHandleRule: DragHandleRule = {
  id: "topLevelOnly",
  evaluate: ({ depth }) => (depth === 1 ? 0 : 1000),
};

/**
 * Use the official nested drag path for exact node boundaries while preserving
 * existing top-level-only drag handle behavior.
 */
export const topLevelDragHandleOptions = {
  defaultRules: false,
  edgeDetection: "none",
  rules: [topLevelDragHandleRule],
} satisfies NestedOptions;
