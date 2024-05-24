import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } =
      await apiClient.extension.storage.policy.listStorageHaloRunV1alpha1Policy();

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
