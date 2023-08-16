import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { radio, radios, defaultIcon } from "@formkit/inputs";
import type { FormKitInputs } from "@formkit/inputs";

declare module "@formkit/inputs" {
  interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    menuRadio: {
      type: "menuRadio";
      value?: string;
    };
  }
}

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await apiClient.extension.menu.listv1alpha1Menu();

    node.props.options = data.items.map((menu) => {
      return {
        value: menu.metadata.name,
        label: menu.spec.displayName,
      };
    });
  });
}

export const menuRadio: FormKitTypeDefinition = {
  ...radio,
  props: ["onValue", "offValue"],
  forceTypeProp: "radio",
  features: [
    optionsHandler,
    radios,
    defaultIcon("decorator", "radioDecorator"),
  ],
};
