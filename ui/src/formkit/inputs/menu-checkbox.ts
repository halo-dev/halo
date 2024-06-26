import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { checkbox, checkboxes, defaultIcon } from "@formkit/inputs";
import { coreApiClient } from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await coreApiClient.menu.listMenu();

    node.props.options = data.items.map((menu) => {
      return {
        value: menu.metadata.name,
        label: menu.spec.displayName,
      };
    });
  });
}

export const menuCheckbox: FormKitTypeDefinition = {
  ...checkbox,
  props: ["onValue", "offValue"],
  forceTypeProp: "checkbox",
  features: [
    optionsHandler,
    checkboxes,
    defaultIcon("decorator", "checkboxDecorator"),
  ],
};
