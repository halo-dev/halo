import type { FormKitNode } from "@formkit/core";
import { findOptions } from "./helpers/findOption";
import type { LabelValueResult } from "./types";

/**
 * Render radio label value
 *
 */
export async function renderRadioLabelValue({
  node,
  value,
}: {
  node: FormKitNode<unknown>;
  value: unknown;
}): Promise<LabelValueResult> {
  const options = node.context?.attrs.options;
  if (!options || options.length === 0) {
    return {
      value,
    };
  }
  const radioSelectedOption = findOptions(options, value);
  if (radioSelectedOption) {
    return {
      value: radioSelectedOption.label,
    };
  }
  return {
    value,
  };
}
