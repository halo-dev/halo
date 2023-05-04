import type { FormKitNode } from "@formkit/core";

const hasLegendNode = (node) =>
  ["checkbox", "radio", "repeater", "group"].includes(node.props.type);

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
        if (hasLegendNode(node)) {
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
