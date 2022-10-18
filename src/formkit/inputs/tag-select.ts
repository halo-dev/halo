import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } =
      await apiClient.extension.tag.listcontentHaloRunV1alpha1Tag();

    node.props.options = data.items.map((tag) => {
      return {
        value: tag.metadata.name,
        label: tag.spec.displayName,
      };
    });
  });
}

export const tagSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
