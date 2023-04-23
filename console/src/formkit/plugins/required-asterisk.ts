import type { FormKitNode } from "@formkit/core";

const isCheckboxAndRadioMultiple = (node) =>
  (node.props.type === "checkbox" || node.props.type === "radio") &&
  node.props.options;

export default function requiredAsterisk(node: FormKitNode) {
  node.on("created", () => {
    if (!node.props.definition) return;

    const schemaFn = node.props.definition?.schema;

    if (typeof schemaFn !== "function") return;

    node.props.definition.schema = (sectionsSchema = {}) => {
      const isRequired = node.props.parsedRules.some(
        (rule) => rule.name === "required"
      );

      if (isRequired) {
        if (isCheckboxAndRadioMultiple(node)) {
          sectionsSchema.legend = {
            children: ["$label", " *"],
          };
        } else {
          sectionsSchema.label = {
            children: ["$label", " *"],
          };
        }
      }

      return schemaFn(sectionsSchema);
    };
  });
}
