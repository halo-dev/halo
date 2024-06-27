import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { defaultIcon, select, selects } from "@formkit/inputs";
import { coreApiClient } from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.storage.group.listGroup({
      labelSelector: ["!halo.run/hidden"],
      sort: ["metadata.creationTimestamp,desc"],
    });

    node.props.options = data.items.map((group) => {
      return {
        value: group.metadata.name,
        label: group.spec.displayName,
      };
    });
  });
}

export const attachmentGroupSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
