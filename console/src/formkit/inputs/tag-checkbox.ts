import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { checkbox, checkboxes, defaultIcon } from "@formkit/inputs";
import type { FormKitInputs } from "@formkit/inputs";

declare module "@formkit/inputs" {
  interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    tagCheckbox: {
      type: "tagCheckbox";
      value?: string[];
    };
  }
}

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } =
      await apiClient.extension.tag.listcontentHaloRunV1alpha1Tag();

    node.props.options = data.items.map((tag) => {
      return {
        value: tag.metadata.name,
        label: tag.spec.displayName,
      };
    });
  });
}

export const tagCheckbox: FormKitTypeDefinition = {
  ...checkbox,
  props: ["onValue", "offValue"],
  forceTypeProp: "checkbox",
  features: [
    optionsHandler,
    checkboxes,
    defaultIcon("decorator", "checkboxDecorator"),
  ],
};
