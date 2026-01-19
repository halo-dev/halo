import { rbacAnnotations } from "@/constants/annotations";
import { roleLabels } from "@/constants/labels";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import { coreApiClient, type Role } from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const result: Role[] = [];
    let page = 1;
    let hasNext = true;

    while (hasNext) {
      const { data } = await coreApiClient.role.listRole({
        page: page,
        size: 1000,
        labelSelector: [`!${roleLabels.TEMPLATE}`],
      });
      result.push(...data.items);
      page++;
      hasNext = data.hasNext;
    }

    const options = [
      ...result.map((role) => {
        return {
          label:
            role.metadata?.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
            role.metadata.name,
          value: role.metadata?.name,
        };
      }),
    ];
    if (node.context) {
      node.context.attrs.options = options;
    }
  });
}

export const roleSelect: FormKitTypeDefinition = {
  ...select,
  forceTypeProp: "select",
  features: [optionsHandler],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    roleSelect: {
      type: "roleSelect";
      value?: string;
    };
  }
}
