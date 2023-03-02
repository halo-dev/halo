import { postLabels } from "@/constants/labels";
import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await apiClient.post.listPosts({
      labelSelector: [
        `${postLabels.DELETED}=false`,
        `${postLabels.PUBLISHED}=true`,
      ],
    });

    node.props.options = data.items.map((post) => {
      return {
        value: post.post.metadata.name,
        label: post.post.spec.title,
      };
    });
  });
}

export const postSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
