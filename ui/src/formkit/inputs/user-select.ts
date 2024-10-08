import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { consoleApiClient } from "@halo-dev/api-client";
import { select } from "./select";

const ANONYMOUSUSER_NAME = "anonymousUser";
const DELETEDUSER_NAME = "ghost";

const search = async ({ page, size, keyword }) => {
  const { data } = await consoleApiClient.user.listUsers({
    page,
    size,
    keyword,
    fieldSelector: [`name!=${ANONYMOUSUSER_NAME}`, `name!=${DELETEDUSER_NAME}`],
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
  forceTypeProp: "select",
  features: [optionsHandler],
};
