import type { FormKitNode } from "@formkit/core";
import { undefine } from "@formkit/utils";

export default function attachmentFeature(node: FormKitNode): void {
  node.on("created", () => {
    node.props.multiple = undefine(node.props.multiple);
    node.props.width = node.props.width ?? "5rem";
    node.props.aspectRatio = node.props.aspectRatio ?? "1/1";
    node.props.accepts = node.props.accepts ?? ["*"];
  });
}
