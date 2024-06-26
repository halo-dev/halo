import { postLabels } from "@/constants/labels";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { defaultIcon, select, selects } from "@formkit/inputs";
import { consoleApiClient } from "@halo-dev/api-client";

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
