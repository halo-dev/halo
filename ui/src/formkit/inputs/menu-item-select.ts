import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import {
  coreApiClient,
  paginate,
  type MenuItem,
  type MenuItemV1alpha1ApiListMenuItemRequest,
} from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const menuItems = await paginate<
      MenuItemV1alpha1ApiListMenuItemRequest,
      MenuItem
    >((params) => coreApiClient.menuItem.listMenuItem(params), {
      fieldSelector: [`name=(${node.props.menuItems.join(",")})`],
    });

    if (node.context) {
      node.context.attrs.options = menuItems.map((menuItem) => {
        return {
          value: menuItem.metadata.name,
          label: menuItem.status?.displayName,
        };
      });
    }
  });
}

export const menuItemSelect: FormKitTypeDefinition = {
  ...select,
  props: ["menuItems", ...(select.props as string[])],
  forceTypeProp: "select",
  features: [optionsHandler],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    menuItemSelect: {
      type: "menuItemSelect";
      value?: string;
    };
  }
}
