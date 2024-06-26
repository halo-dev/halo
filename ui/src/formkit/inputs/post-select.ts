import { postLabels } from "@/constants/labels";
import { consoleApiClient } from "@halo-dev/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await consoleApiClient.content.post.listPosts({
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
