import { singlePageLabels } from "@/constants/labels";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import {
  consoleApiClient,
  paginate,
  type ListedSinglePage,
  type SinglePageV1alpha1ConsoleApiListSinglePagesRequest,
} from "@halo-dev/api-client";
import { select } from "./select";

async function search({ page, size, keyword }) {
  const { data } = await consoleApiClient.content.singlePage.listSinglePages({
    page,
    size,
    keyword,
    labelSelector: [
      `${singlePageLabels.DELETED}=false`,
      `${singlePageLabels.PUBLISHED}=true`,
    ],
  });

  return {
    options: data.items.map((singlePage) => {
      return {
        value: singlePage.page.metadata.name,
        label: singlePage.page.spec.title,
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

  const singlePages = await paginate<
    SinglePageV1alpha1ConsoleApiListSinglePagesRequest,
    ListedSinglePage
  >((params) => consoleApiClient.content.singlePage.listSinglePages(params), {
    fieldSelector: [`metadata.name=(${values.join(",")})`],
    size: 1000,
  });

  return singlePages.map((singlePage) => {
    return {
      value: singlePage.page.metadata.name,
      label: singlePage.page.spec.title,
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

export const singlePageSelect: FormKitTypeDefinition = {
  ...select,
  forceTypeProp: "select",
  features: [optionsHandler],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    singlePageSelect: {
      type: "singlePageSelect";
      value?: string;
    };
  }
}
