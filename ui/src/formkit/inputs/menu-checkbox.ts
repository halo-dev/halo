import { paginate } from "@/utils/paginate";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import {
  checkbox,
  checkboxes,
  defaultIcon,
  type FormKitInputs,
} from "@formkit/inputs";
import {
  coreApiClient,
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

export const menuCheckbox: FormKitTypeDefinition = {
  ...checkbox,
  props: ["onValue", "offValue"],
  forceTypeProp: "checkbox",
  features: [
    optionsHandler,
    checkboxes,
    defaultIcon("decorator", "checkboxDecorator"),
  ],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    menuCheckbox: {
      type: "menuCheckbox";
      value?: string[];
    };
  }
}
