import type { FormKitNode } from "@formkit/core";
import type { LabelValueResult } from "./types";

export async function renderAttachmentLabelValue({
  value,
}: {
  node: FormKitNode<unknown>;
  value: unknown;
}): Promise<LabelValueResult | LabelValueResult[]> {
  const castValueArray = Array.isArray(value) ? value : [value];
  return castValueArray.map((v) => {
    return {
      value: v,
    };
  });
}
