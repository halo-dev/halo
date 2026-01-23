import type { FormKitNode } from "@formkit/core";
import { findOptions } from "./helpers/findOption";
import type { LabelValueResult } from "./types";

/**
 * Render category select label value
 *
 */
export async function renderCategorySelectLabelValue({
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
    const categorySelectSelectedOption = findOptions(options, value);
    if (categorySelectSelectedOption) {
      return {
        value: categorySelectSelectedOption.label,
      };
    }
  }

  if (Array.isArray(value)) {
    return {
      value: value.map((v) => {
        const categorySelectSelectedOption = findOptions(options, v);
        return categorySelectSelectedOption?.label;
      }),
    };
  }

  return {
    value,
  };
}
