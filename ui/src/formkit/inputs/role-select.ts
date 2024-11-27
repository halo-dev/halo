import { rbacAnnotations } from "@/constants/annotations";
import { roleLabels } from "@/constants/labels";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { coreApiClient } from "@halo-dev/api-client";
import { select } from "./select";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.role.listRole({
      page: 0,
      size: 0,
      labelSelector: [`!${roleLabels.TEMPLATE}`],
    });

    const options = [
      ...data.items.map((role) => {
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
