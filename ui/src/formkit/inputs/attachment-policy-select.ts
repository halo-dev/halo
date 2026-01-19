import { paginate } from "@/utils/paginate";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import {
  coreApiClient,
  type Policy,
  type PolicyV1alpha1ApiListPolicyRequest,
} from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const policies = await paginate<PolicyV1alpha1ApiListPolicyRequest, Policy>(
      (params) => coreApiClient.storage.policy.listPolicy(params),
      {
        size: 1000,
      }
    );

    if (node.context) {
      node.context.attrs.options = policies.map((policy) => {
        return {
          value: policy.metadata.name,
          label: policy.spec.displayName,
        };
      });
    }
  });
}

export const attachmentPolicySelect: FormKitTypeDefinition = {
  ...select,
  forceTypeProp: "select",
  features: [optionsHandler],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachmentPolicySelect: {
      type: "attachmentPolicySelect";
      value?: string;
    };
  }
}
