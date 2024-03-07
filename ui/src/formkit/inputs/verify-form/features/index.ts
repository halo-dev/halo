import { i18n } from "@/locales";
import { createMessage, type FormKitNode } from "@formkit/core";

/**
 * Set the incomplete message on a specific node.
 * @param node - The node to set the incomplete message on.
 */
export function setIncompleteMessage(node: FormKitNode) {
  node.store.set(
    createMessage({
      blocking: false,
      key: `incomplete`,
      meta: {
        localize: node.props.incompleteMessage === undefined,
        i18nArgs: [{ node }],
        showAsMessage: true,
      },
      type: "ui",
      value: node.props.incompleteMessage || "Form incomplete.",
    })
  );
}

/**
 * A feature to add a submit handler and actions section.
 *
 * @param node - A {@link @formkit/core#FormKitNode | FormKitNode}.
 *
 * @public
 */
export default function form(node: FormKitNode): void {
  node.props.isForm = true;
  node.ledger.count("validating", (m) => m.key === "validating");

  node.props.submitAttrs ??= {
    disabled: node.props.disabled,
  };

  node.props.submitLabel ??=
    node.props.label || i18n.global.t("core.common.buttons.verify");

  node.on("prop:disabled", ({ payload: disabled }) => {
    node.props.submitAttrs = { ...node.props.submitAttrs, disabled };
  });

  node.on("created", () => {
    if (node.parent) {
      node.parent.hook.commit((val) => {
        const verifyFormVal = val[node.name] || {};
        delete val[node.name];
        return { ...val, ...verifyFormVal };
      });
      node.input(node.parent.value);
    }
  });
  node.on("prop:incompleteMessage", () => {
    if (node.store.incomplete) setIncompleteMessage(node);
  });
  node.on("settled:blocking", () => node.store.remove("incomplete"));
}
