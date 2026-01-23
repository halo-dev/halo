import type { FormKitNode } from "@formkit/core";
import { findOptions } from "./helpers/findOption";
import type { LabelValueResult } from "./types";

/**
 * Render tag select label value
 *
 */
export async function renderTagSelectLabelValue({
  node,
  value,
}: {
  node: FormKitNode<unknown>;
  value: unknown;
}): Promise<LabelValueResult> {
  const options = node.context?.options;
  if (!options || options.length === 0) {
    return {
      value,
    };
  }

  if (typeof value === "string") {
    const tagSelectSelectedOption = findOptions(options, value);
    if (tagSelectSelectedOption) {
      return {
        value: tagSelectSelectedOption.label,
      };
    }
  }

  if (Array.isArray(value)) {
    return {
      value: value.map((v) => {
        const tagSelectSelectedOption = findOptions(options, v);
        return tagSelectSelectedOption?.label;
      }),
    };
  }

  return {
    value,
  };
}
