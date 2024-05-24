import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { checkbox, checkboxes, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } =
      await apiClient.extension.category.listContentHaloRunV1alpha1Category({
        sort: ["metadata.creationTimestamp,desc"],
      });

    node.props.options = data.items.map((category) => {
      return {
        value: category.metadata.name,
        label: category.spec.displayName,
      };
    });
  });
}

export const categoryCheckbox: FormKitTypeDefinition = {
  ...checkbox,
  props: ["onValue", "offValue"],
  forceTypeProp: "checkbox",
  features: [
    optionsHandler,
    checkboxes,
    defaultIcon("decorator", "checkboxDecorator"),
  ],
};
