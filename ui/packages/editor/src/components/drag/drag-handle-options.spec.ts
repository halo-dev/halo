import type {
  DragHandleRule,
  RuleContext,
} from "@tiptap/extension-drag-handle";
import { describe, expect, it } from "vitest";
import type { PMNode } from "@/tiptap";
import {
  BLOCK_DRAG_HANDLE_OFFSET,
  blockDragHandleBridgeStyle,
  blockDragHandleOptions,
  blockDragHandleRule,
} from "./drag-handle-options";

describe("blockDragHandleRule", () => {
  it("keeps top-level blocks draggable", () => {
    expect(evaluateRule({ depth: 1, nodeType: "codeBlock" })).toBe(0);
    expect(evaluateRule({ depth: 1, nodeType: "orderedList" })).toBe(0);
  });

  it("keeps the leading list paragraph attached to its list row", () => {
    expect(
      evaluateRule({
        depth: 3,
        nodeType: "paragraph",
        parentType: "listItem",
        isFirst: true,
      })
    ).toBe(1000);
  });

  it("allows a standalone block after the leading list paragraph", () => {
    expect(
      evaluateRule({
        depth: 3,
        nodeType: "codeBlock",
        parentType: "listItem",
        isFirst: false,
      })
    ).toBe(0);
  });

  it("keeps nested list wrappers attached to the surrounding list", () => {
    expect(
      evaluateRule({
        depth: 3,
        nodeType: "orderedList",
        parentType: "listItem",
        firstChildType: "listItem",
        isFirst: false,
      })
    ).toBe(1000);
  });

  it("does not expose arbitrary deeper descendants", () => {
    expect(
      evaluateRule({
        depth: 4,
        nodeType: "paragraph",
        parentType: "blockquote",
        isFirst: true,
      })
    ).toBe(1000);
  });

  it("uses the official nested path with Halo's focused rule", () => {
    expect(blockDragHandleOptions).toMatchObject({
      defaultRules: false,
      edgeDetection: "none",
      rules: [blockDragHandleRule],
    });
  });

  it("bridges the visual gap without moving the handle against the block", () => {
    expect(BLOCK_DRAG_HANDLE_OFFSET).toBe(5);
    expect(blockDragHandleBridgeStyle).toEqual({
      "--halo-drag-handle-hover-bridge": "5px",
    });
  });
});

function evaluateRule({
  depth,
  nodeType,
  parentType,
  firstChildType,
  isFirst = false,
}: {
  depth: number;
  nodeType: string;
  parentType?: string;
  firstChildType?: string;
  isFirst?: boolean;
}) {
  const node = {
    type: mockNodeType(nodeType),
    firstChild: firstChildType ? { type: mockNodeType(firstChildType) } : null,
  } as unknown as PMNode;
  const parent = parentType
    ? ({ type: mockNodeType(parentType) } as unknown as PMNode)
    : null;
  const context = {
    node,
    parent,
    depth,
    isFirst,
  } as RuleContext;
  return (blockDragHandleRule as DragHandleRule).evaluate(context);
}

function mockNodeType(name: string) {
  return {
    name,
    isInGroup: (group: string) =>
      group === "listItem" && (name === "listItem" || name === "taskItem"),
  };
}
