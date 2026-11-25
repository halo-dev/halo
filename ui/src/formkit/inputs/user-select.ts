import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import {
  consoleApiClient,
  paginate,
  type ListedUser,
  type UserV1alpha1ConsoleApiListUsersRequest,
} from "@halo-dev/api-client";
import { applyDataPermissionGuard } from "@/formkit/utils/permission";
import { select } from "./select";

const requiredPermissions = ["system:users:view", "system:users:manage"];

const ANONYMOUSUSER_NAME = "anonymousUser";
const DELETEDUSER_NAME = "ghost";

interface SearchParams {
  page?: number;
  size?: number;
  keyword?: string;
  effectiveRole?: string;
}

const search = async ({ page, size, keyword, effectiveRole }: SearchParams) => {
  const { data } = await consoleApiClient.user.listUsers({
    page,
    size,
    keyword,
    effectiveRole,
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
    if (!applyDataPermissionGuard(node, requiredPermissions)) {
      return;
    }

    const effectiveRole = node.props.effectiveRole as string | undefined;
    node.props = {
      ...node.props,
      remote: true,
      // Disable the remote-optimize cache so selected values outside the filtered
      // result set still resolve through findOptionsByValues instead of the cache.
      remoteOptimize: false,
      remoteOption: {
        // Filter search results by effective role while keeping already selected values
        // resolvable via findOptionsByValues, so existing owners are still displayed.
        search: (params: SearchParams) => search({ ...params, effectiveRole }),
        findOptionsByValues,
      },
      searchable: true,
    };
  });
}

export const userSelect: FormKitTypeDefinition = {
  ...select,
  forceTypeProp: "select",
  props: [...(select.props as string[]), "effectiveRole"],
  features: [optionsHandler],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    userSelect: {
      type: "userSelect";
      value?: string;
      effectiveRole?: string;
    };
  }
}
