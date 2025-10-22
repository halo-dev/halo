import type { Node } from "@tiptap/pm/model";

export const isEmpty = (node: Node) => {
  return isNodeDefault(node) || isParagraphEmpty(node);
};

export const isNodeDefault = (node: Node) => {
  const defaultContent = node.type.createAndFill()?.toJSON();
  const content = node.toJSON();
  return JSON.stringify(defaultContent) === JSON.stringify(content);
};

export const isParagraphEmpty = (node: Node) => {
  if (node.type.name !== "paragraph") {
    return false;
  }

  if (node.childCount > 0) {
    return false;
  }

  return node.textContent.length === 0;
};
