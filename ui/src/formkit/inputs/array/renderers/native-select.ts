import type { FormKitNode } from "@formkit/core";
import { findOptions } from "./helpers/findOption.js";
import type { LabelValueResult } from "./types.js";

/**
 * Render native select type label value
 */
export function renderNativeSelectLabelValue({
  node,
  value,
}: {
  node: FormKitNode<unknown>;
  value: unknown;
}): LabelValueResult {
  const options = node.context?.options as unknown[] | Record<string, string>;

  if (typeof value === "string") {
    const selectedOption = findOptions(options, value);
    if (selectedOption) {
      return {
        value: selectedOption.group
          ? `${selectedOption.group}/${selectedOption.label}`
          : selectedOption.label,
      };
    }
  }

  if (Array.isArray(value)) {
    return {
      value: value.map((v) => {
        const selectedOption = findOptions(options, v);
        return selectedOption?.label;
      }),
    };
  }

  return {
    value,
  };
}
