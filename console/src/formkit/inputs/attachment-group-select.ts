import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";
import type { FormKitInputs } from "@formkit/inputs";

declare module "@formkit/inputs" {
  interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachmentGroupSelect: {
      type: "attachmentGroupSelect";
      value?: string;
    };
  }
}

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } =
      await apiClient.extension.storage.group.liststorageHaloRunV1alpha1Group({
        labelSelector: ["!halo.run/hidden"],
      });

    node.props.options = data.items.map((group) => {
      return {
        value: group.metadata.name,
        label: group.spec.displayName,
      };
    });
  });
}

export const attachmentGroupSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
