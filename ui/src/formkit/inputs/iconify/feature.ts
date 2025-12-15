import type { FormKitNode } from "@formkit/core";

/**
 * Ensure that the content entered for iconify is correctly set, and use the default value when it is not set.
 * @param node
 */
export default function iconifyFeature(node: FormKitNode): void {
  node.on("created", () => {
    node.props.format = node.props.format ?? "svg";
  });
}
