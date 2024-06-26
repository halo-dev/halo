import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { defaultIcon, select, selects } from "@formkit/inputs";
import { coreApiClient } from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.menuItem.listMenuItem({
      fieldSelector: [`name=(${node.props.menuItems.join(",")})`],
    });

    node.props.options = data.items.map((menuItem) => {
      return {
        value: menuItem.metadata.name,
        label: menuItem.status?.displayName,
      };
    });
  });
}

export const menuItemSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder", "menuItems"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
