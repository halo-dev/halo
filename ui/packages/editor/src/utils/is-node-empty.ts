import type { Node } from "@tiptap/pm/model";

export const isEmpty = (node: Node) => {
  return isNodeDefault(node) || isNodeContentEmpty(node);
};

export const isNodeDefault = (node: Node) => {
  const defaultContent = node.type.createAndFill()?.toJSON();
  const content = node.toJSON();
  return JSON.stringify(defaultContent) === JSON.stringify(content);
};

export const isNodeContentEmpty = (node: Node) => {
  if (node.isTextblock) {
    return node.textContent.length === 0;
  }

  if (node.childCount === 0) {
    return true;
  }

  let allChildrenEmpty = true;
  node.forEach((child) => {
    if (!isEmpty(child)) {
      allChildrenEmpty = false;
    }
  });

  return allChildrenEmpty;
};

export const isParagraphEmpty = (node: Node) => {
  if (node.type.name !== "paragraph") {
    return false;
  }

  return node.textContent.length === 0;
};

export const isBlockEmpty = (node: Node) => {
  if (!node.isTextblock) {
    return false;
  }

  return isParagraphEmpty(node);
};
