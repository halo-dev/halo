import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await apiClient.extension.menuItem.listv1alpha1MenuItem({
      fieldSelector: [`name=(${node.props.menuItems.join(",")})`],
    });

    node.props.options = data.items.map((menuItem) => {
      return {
        value: menuItem.metadata.name,
        label: menuItem.status?.displayName,
      };
    });
  });
}

export const menuItemSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder", "menuItems"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
