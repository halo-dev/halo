import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { coreApiClient } from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.menu.listMenu();

    if (node.context) {
      node.context.attrs.options = data.items.map((menu) => {
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
