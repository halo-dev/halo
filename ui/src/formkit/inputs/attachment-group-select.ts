import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } =
      await apiClient.extension.storage.group.listStorageHaloRunV1alpha1Group({
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
