import type { FormKitNode } from "@formkit/core";
import { findOptions } from "./helpers/findOption";
import type { LabelValueResult } from "./types";

export async function renderToggleLabelValue({
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
    const toggleSelectedOption = findOptions(options, value);
    if (toggleSelectedOption) {
      return {
        value: toggleSelectedOption.label,
      };
    }
  }

  if (Array.isArray(value)) {
    return {
      value: value.map((v) => {
        const toggleSelectedOption = findOptions(options, v);
        return toggleSelectedOption?.label;
      }),
    };
  }

  return {
    value,
  };
}
