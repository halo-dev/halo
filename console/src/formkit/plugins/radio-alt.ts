import type { FormKitNode } from "@formkit/core";

let i = 0;

export default function radioAlt(node: FormKitNode) {
  if (node.props.type === "radio") {
    node.props.altName = `radio_${node.name}_${++i}`;
  }
}
