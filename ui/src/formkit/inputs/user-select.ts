// TODO: This is a temporary approach.
// We will provide searchable user selection components in the future.

import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { defaultIcon, select, selects } from "@formkit/inputs";
import { coreApiClient } from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.user.listUser();

    node.props.options = data.items.map((user) => {
      return {
        value: user.metadata.name,
        label: user.spec.displayName,
      };
    });
  });
}

export const userSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
