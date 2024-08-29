import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { coreApiClient } from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.storage.group.listGroup({
      labelSelector: ["!halo.run/hidden"],
      sort: ["metadata.creationTimestamp,desc"],
    });

    if (node.context) {
      node.context.attrs.options = data.items.map((group) => {
        return {
          value: group.metadata.name,
          label: group.spec.displayName,
        };
      });
    }
  });
}

export const attachmentGroupSelect: FormKitTypeDefinition = {
  ...select,
  forceTypeProp: "select",
  features: [optionsHandler],
};
