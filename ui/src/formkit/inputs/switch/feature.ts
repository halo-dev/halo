import type { FormKitNode } from "@formkit/core";
import { normalizeBoxes } from "@formkit/inputs";
import { has, undefine } from "@formkit/utils";

/**
 * A feature that adds switch selection support.
 *
 * @param node - A {@link @formkit/core#FormKitNode | FormKitNode}.
 *
 * @public
 */
export default function switchFeature(node: FormKitNode): void {
  node.on("created", () => {
    if (!has(node.props, "onValue")) {
      node.props.onValue = true;
    }
    if (!has(node.props, "offValue")) {
      node.props.offValue = false;
    }

    node.props.disabled = undefine(node.props.disabled);
  });

  node.hook.prop(normalizeBoxes(node));
}
