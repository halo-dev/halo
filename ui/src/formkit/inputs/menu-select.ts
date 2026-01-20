import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import {
  coreApiClient,
  paginate,
  type Menu,
  type MenuV1alpha1ApiListMenuRequest,
} from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const menus = await paginate<MenuV1alpha1ApiListMenuRequest, Menu>(
      (params) => coreApiClient.menu.listMenu(params),
      {
        size: 1000,
      }
    );

    if (node.context) {
      node.context.attrs.options = menus.map((menu) => {
        return {
          value: menu.metadata.name,
          label: menu.spec.displayName,
        };
      });
    }
  });
}

export const menuSelect: FormKitTypeDefinition = {
  ...select,
  props: [...(select.props as string[])],
  forceTypeProp: "select",
  features: [optionsHandler],
};
