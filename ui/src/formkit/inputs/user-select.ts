// TODO: This is a temporary approach.
// We will provide searchable user selection components in the future.

import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { consoleApiClient } from "@halo-dev/api-client";
import { select } from "./select";

const search = async ({ page, size, keyword }) => {
  const { data } = await consoleApiClient.user.listUsers({
    page,
    size,
    keyword,
  });
  return {
    options: data.items?.map((user) => {
      return {
        value: user.user.metadata.name,
        label: user.user.spec.displayName,
      };
    }),
    total: data.total,
    size: data.size,
    page: data.page,
  };
};

const findOptionsByValues = async (values: string[]) => {
  if (values.length === 0) {
    return [];
  }

  const { data } = await consoleApiClient.user.listUsers({
    fieldSelector: [`metadata.name=(${values.join(",")})`],
  });

  return data.items?.map((user) => {
    return {
      value: user.user.metadata.name,
      label: user.user.spec.displayName,
    };
  });
};

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

export const userSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler],
};
