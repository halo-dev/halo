import { postLabels } from "@/constants/labels";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { consoleApiClient } from "@halo-dev/api-client";
import { select } from "./select";

async function search({ page, size, keyword }) {
  const { data } = await consoleApiClient.content.post.listPosts({
    page,
    size,
    keyword,
    labelSelector: [
      `${postLabels.DELETED}=false`,
      `${postLabels.PUBLISHED}=true`,
    ],
  });

  return {
    options: data.items.map((post) => {
      return {
        value: post.post.metadata.name,
        label: post.post.spec.title,
      };
    }),
    total: data.total,
    size: data.size,
    page: data.page,
  };
}

async function findOptionsByValues(values: string[]) {
  if (values.length === 0) {
    return [];
  }

  const { data } = await consoleApiClient.content.post.listPosts({
    fieldSelector: [`metadata.name=(${values.join(",")})`],
  });

  return data.items.map((post) => {
    return {
      value: post.post.metadata.name,
      label: post.post.spec.title,
    };
  });
}

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    node.props = {
      ...node.props,
      remote: true,
      remoteOption: {
        search,
        findOptionsByValues,
      },
      searchable: true,
    };
  });
}

export const postSelect: FormKitTypeDefinition = {
  ...select,
  forceTypeProp: "select",
  features: [optionsHandler],
};
