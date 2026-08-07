import type { Attribute } from "@tiptap/core";
import { mergeAttributes } from "@/tiptap";
import {
  joinStyles,
  parseCellBackgroundColor,
  parseCellVerticalAlign,
  parseColumnWidths,
} from "./attributes";

export function createTableCellAttributes(
  parentAttributes: Record<string, Attribute>
) {
  return {
    ...parentAttributes,
    colwidth: {
      ...parentAttributes.colwidth,
      default: null,
      parseHTML: parseColumnWidths,
    },
    verticalAlign: {
      default: null,
      parseHTML: parseCellVerticalAlign,
      renderHTML: () => ({}),
    },
    backgroundColor: {
      default: null,
      parseHTML: parseCellBackgroundColor,
      renderHTML: () => ({}),
    },
  };
}

export function renderTableCellAttributes(
  configuredAttributes: Record<string, unknown>,
  htmlAttributes: Record<string, unknown>,
  nodeAttributes: Record<string, unknown>
) {
  const verticalAlign = nodeAttributes.verticalAlign as string | null;
  const backgroundColor = nodeAttributes.backgroundColor as string | null;
  const attributes = mergeAttributes(
    configuredAttributes,
    htmlAttributes,
    verticalAlign ? { "data-vertical-align": verticalAlign } : {},
    backgroundColor ? { "data-background-color": backgroundColor } : {}
  );

  attributes.style = joinStyles(
    htmlAttributes.style as string | undefined,
    verticalAlign && `vertical-align: ${verticalAlign}`,
    backgroundColor && `background-color: ${backgroundColor}`
  );
  if (!attributes.style) {
    delete attributes.style;
  }

  return attributes;
}
