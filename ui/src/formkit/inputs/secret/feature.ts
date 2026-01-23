import type { FormKitNode } from "@formkit/core";

export default function secretFeature(node: FormKitNode): void {
  node.on("created", () => {
    node.props.requiredKeys = node.props.requiredKeys ?? [];
  });
}
