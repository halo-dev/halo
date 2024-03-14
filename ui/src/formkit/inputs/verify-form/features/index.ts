import { i18n } from "@/locales";
import { type FormKitNode } from "@formkit/core";

function buildVerifyFormValue(node: FormKitNode) {
  if (!node.parent) return {};
  const parentValue = {
    ...(node.parent.value as Record<string, unknown>),
  };
  delete parentValue[node.name];
  return parentValue;
}

/**
 * A feature to add a submit handler and actions section.
 *
 * @param node - A {@link @formkit/core#FormKitNode | FormKitNode}.
 *
 * @public
 */
export default function verify(node: FormKitNode): void {
  node.props.buttonAttrs ??= {
    disabled: node.props.disabled,
  };

  node.props.label ??= i18n.global.t("core.common.buttons.verify");

  node.on("prop:disabled", ({ payload: disabled }) => {
    node.props.buttonAttrs = { ...node.props.buttonAttrs, disabled };
  });

  node.on("created", () => {
    if (node.parent) {
      node.parent.hook.commit((val) => {
        const parentValue = {
          ...val,
        };
        const verifyFormVal = (parentValue[node.name] || {}) as Record<
          string,
          unknown
        >;
        delete parentValue[node.name];
        const mergeFormValue = {};
        Object.keys(verifyFormVal).forEach((key) => {
          if (node.children.find((child) => child.name === key)) {
            mergeFormValue[key] = verifyFormVal[key];
          }
        });
        return { ...parentValue, ...mergeFormValue };
      });

      node.hook.input(() => {
        return buildVerifyFormValue(node);
      });

      node.input(buildVerifyFormValue(node));
    }
  });
}
