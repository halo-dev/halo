import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import {
  defaultIcon,
  radio,
  radios,
  type FormKitInputs,
} from "@formkit/inputs";
import {
  coreApiClient,
  paginate,
  type Menu,
  type MenuV1alpha1ApiListMenuRequest,
} from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const menus = await paginate<MenuV1alpha1ApiListMenuRequest, Menu>(
      (params) => coreApiClient.menu.listMenu(params),
      {
        size: 1000,
      }
    );

    node.props.options = menus.map((menu) => {
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

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    menuRadio: {
      type: "menuRadio";
      value?: string;
    };
  }
}
