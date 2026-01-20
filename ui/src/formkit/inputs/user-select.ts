import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import {
  consoleApiClient,
  paginate,
  type ListedUser,
  type UserV1alpha1ConsoleApiListUsersRequest,
} from "@halo-dev/api-client";
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
        label: `${user.user.spec.displayName}(${user.user.metadata.name})`,
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

  const users = await paginate<
    UserV1alpha1ConsoleApiListUsersRequest,
    ListedUser
  >((params) => consoleApiClient.user.listUsers(params), {
    fieldSelector: [`metadata.name=(${values.join(",")})`],
    size: 1000,
  });

  return users.map((user) => {
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

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    userSelect: {
      type: "userSelect";
      value?: string;
    };
  }
}
