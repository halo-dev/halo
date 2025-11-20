import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import { coreApiClient } from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.storage.policy.listPolicy();

    if (node.context) {
      node.context.attrs.options = data.items.map((policy) => {
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
