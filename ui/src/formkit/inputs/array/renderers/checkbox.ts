import type { FormKitNode } from "@formkit/core";
import { findOptions } from "./helpers/findOption";
import type { LabelValueResult } from "./types";

/**
 * Render checkbox label value
 *
 */
export async function renderCheckboxLabelValue({
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

  if (typeof value === "string") {
    const checkboxSelectedOption = findOptions(options, value);
    if (checkboxSelectedOption) {
      return {
        value: checkboxSelectedOption.label,
      };
    }
  }

  if (Array.isArray(value)) {
    return {
      value: value.map((v) => {
        const checkboxSelectedOption = findOptions(options, v);
        return checkboxSelectedOption?.label;
      }),
    };
  }

  return {
    value,
  };
}
