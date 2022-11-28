import { singlePageLabels } from "@/constants/labels";
import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await apiClient.singlePage.listSinglePages({
      labelSelector: [
        `${singlePageLabels.DELETED}=false`,
        `${singlePageLabels.PUBLISHED}=true`,
      ],
    });

    node.props.options = data.items.map((singlePage) => {
      return {
        value: singlePage.page.metadata.name,
        label: singlePage.page.spec.title,
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
