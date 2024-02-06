import type { FormKitNode } from "@formkit/core";

export default function passwordPreventAutocomplete(node: FormKitNode) {
  if (node.props.type === "password" && !node.props.attrs?.autocomplete) {
    if (!node.props.attrs) node.props.attrs = {};
    // https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion#preventing_autofilling_with_autocompletenew-password
    node.props.attrs.autocomplete = "new-password";
  }
}
