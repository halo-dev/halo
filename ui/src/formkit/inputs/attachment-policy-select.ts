import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { defaultIcon, select, selects } from "@formkit/inputs";
import { coreApiClient } from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.storage.policy.listPolicy();

    node.props.options = data.items.map((policy) => {
      return {
        value: policy.metadata.name,
        label: policy.spec.displayName,
      };
    });
  });
}

export const attachmentPolicySelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
