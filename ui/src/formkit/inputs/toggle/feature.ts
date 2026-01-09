import type { FormKitNode } from "@formkit/core";
import { undefine } from "@formkit/utils";

export default function toggleFeature(node: FormKitNode): void {
  node.on("created", () => {
    node.props.multiple = undefine(node.props.multiple) ?? false;
    node.props.renderType = node.props.renderType ?? "text";

    if (node.props.multiple) {
      if (node.value === undefined) {
        node.input([], false);
      }
    }

    if (node.context) {
      node.context.initialValue = node.value || "";
    }
  });
}
