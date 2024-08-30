import { PMNode } from "@/tiptap";

export const isEmpty = (node: PMNode) => {
  return isNodeDefault(node) || isParagraphEmpty(node);
};

export const isNodeDefault = (node: PMNode) => {
  const defaultContent = node.type.createAndFill()?.toJSON();
  const content = node.toJSON();
  return JSON.stringify(defaultContent) === JSON.stringify(content);
};

export const isParagraphEmpty = (node: PMNode) => {
  if (node.type.name !== "paragraph") {
    return false;
  }

  if (node.childCount > 0) {
    return false;
  }

  return node.textContent.length === 0;
};
