import type { FormKitNode } from "@formkit/core";
import type { LabelValueResult } from "./types";

/**
 * Render iconify type label value
 */
export function renderIconifyLabelValue({
  node,
}: {
  node: FormKitNode<unknown>;
}): LabelValueResult {
  const format = node.props.format;
  const valueOnly = node.props.valueOnly;
  return {
    format,
    valueOnly,
  };
}
