import type { FormKitNode } from "@formkit/core";

export type LabelValueResult = { value?: unknown } & Record<string, unknown>;

export type LabelValueRenderer = (params: {
  node: FormKitNode<unknown>;
  value: unknown;
}) => Promise<LabelValueResult> | LabelValueResult;
