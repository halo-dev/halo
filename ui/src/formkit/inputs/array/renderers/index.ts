import type { FormKitNode } from "@formkit/core";
import { renderCategorySelectLabelValue } from "./category-select";
import { renderCheckboxLabelValue } from "./checkbox";
import { renderIconifyLabelValue } from "./iconify";
import { renderNativeSelectLabelValue } from "./native-select";
import { renderRadioLabelValue } from "./radio";
import { renderSelectLabelValue } from "./select";
import { renderTagSelectLabelValue } from "./tag-select";
import { renderToggleLabelValue } from "./toggle";
import type { LabelValueRenderer, LabelValueResult } from "./types";

/**
 * Default renderer for unknown types
 */
const defaultRenderer: LabelValueRenderer = ({
  value,
}: {
  value: unknown;
}): LabelValueResult => {
  return {
    value: String(value),
  };
};

/**
 * Renderer registry
 */
const rendererRegistry = new Map<string, LabelValueRenderer>([
  ["select", renderSelectLabelValue],
  ["nativeSelect", renderNativeSelectLabelValue],
  ["iconify", renderIconifyLabelValue],
  ["checkbox", renderCheckboxLabelValue],
  ["radio", renderRadioLabelValue],
  ["toggle", renderToggleLabelValue],
  ["tagSelect", renderTagSelectLabelValue],
  ["categorySelect", renderCategorySelectLabelValue],
]);

/**
 * Register a custom renderer for a specific node type
 */
export function registerRenderer(
  nodeType: string,
  renderer: LabelValueRenderer
): void {
  rendererRegistry.set(nodeType, renderer);
}

/**
 * Render item label value based on node type
 */
export async function renderItemLabelValue(
  node: FormKitNode<unknown>,
  value: unknown
): Promise<LabelValueResult> {
  const originalType = node.props.originalType;
  const renderer = rendererRegistry.get(originalType);
  if (renderer) {
    return await renderer({ node, value });
  }
  const nodeType = node.props.type as string;
  const typeNodeRenderer = rendererRegistry.get(nodeType) || defaultRenderer;
  return await typeNodeRenderer({ node, value });
}

export type { LabelValueRenderer, LabelValueResult };
