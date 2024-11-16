import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { coreApiClient } from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.menuItem.listMenuItem({
      fieldSelector: [`name=(${node.props.menuItems.join(",")})`],
    });

    if (node.context) {
      node.context.attrs.options = data.items.map((menuItem) => {
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
