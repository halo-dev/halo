import type { FormKitNode } from "@formkit/core";

export default function autoScrollToErrors(node: FormKitNode) {
  const scrollTo = (node: FormKitNode) => {
    if (!node.props.id) {
      return;
    }
    const el = document.getElementById(node.props.id);
    if (el) {
      el.scrollIntoView({ block: "end", inline: "nearest" });
    }
  };

  const scrollToErrors = () => {
    node.walk((child) => {
      if (child.ledger.value("blocking") || child.ledger.value("errors")) {
        scrollTo(child);
        return false;
      }
    }, true);
  };

  if (node.props.type === "form") {
    const onOldSubmitInvalid = node.props.onSubmitInvalid;
    node.props.onSubmitInvalid = () => {
      if (onOldSubmitInvalid) {
        onOldSubmitInvalid(node);
      }
      scrollToErrors();
    };
    node.on("unsettled:errors", scrollToErrors);
  }
  return false;
}
