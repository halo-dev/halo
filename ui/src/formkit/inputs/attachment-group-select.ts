import { paginate } from "@/utils/paginate";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import {
  coreApiClient,
  type Group,
  type GroupV1alpha1ApiListGroupRequest,
} from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const groups = await paginate<GroupV1alpha1ApiListGroupRequest, Group>(
      (params) => coreApiClient.storage.group.listGroup(params),
      {
        labelSelector: ["!halo.run/hidden"],
        sort: ["metadata.creationTimestamp,desc"],
      }
    );

    if (node.context) {
      node.context.attrs.options = groups.map((group) => {
        return {
          value: group.metadata.name,
          label: group.spec.displayName,
        };
      });
    }
  });
}

export const attachmentGroupSelect: FormKitTypeDefinition = {
  ...select,
  forceTypeProp: "select",
  features: [optionsHandler],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachmentGroupSelect: {
      type: "attachmentGroupSelect";
      value?: string;
    };
  }
}
