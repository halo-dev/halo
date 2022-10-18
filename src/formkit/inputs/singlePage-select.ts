import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } =
      await apiClient.extension.singlePage.listcontentHaloRunV1alpha1SinglePage();

    node.props.options = data.items.map((singlePage) => {
      return {
        value: singlePage.metadata.name,
        label: singlePage.spec.title,
      };
    });
  });
}

export const singlePageSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
