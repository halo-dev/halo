import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { defaultIcon, radio, radios } from "@formkit/inputs";
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

export const menuRadio: FormKitTypeDefinition = {
  ...radio,
  props: ["onValue", "offValue"],
  forceTypeProp: "radio",
  features: [
    optionsHandler,
    radios,
    defaultIcon("decorator", "radioDecorator"),
  ],
};
