import { rbacAnnotations } from "@/constants/annotations";
import { roleLabels } from "@/constants/labels";
import { apiClient } from "@/utils/api-client";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import { select, selects, defaultIcon } from "@formkit/inputs";
import { i18n } from "@/locales";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const { data } = await apiClient.extension.role.listv1alpha1Role({
      page: 0,
      size: 0,
      labelSelector: [`!${roleLabels.TEMPLATE}`],
    });

    node.props.options = [
      {
        label: i18n.global.t(
          "core.user.grant_permission_modal.fields.role.placeholder"
        ),
        value: "",
      },
      ...data.items.map((role) => {
        return {
          label:
            role.metadata?.annotations?.[rbacAnnotations.DISPLAY_NAME] ||
            role.metadata.name,
          value: role.metadata?.name,
        };
      }),
    ];
  });
}

export const roleSelect: FormKitTypeDefinition = {
  ...select,
  props: ["placeholder"],
  forceTypeProp: "select",
  features: [optionsHandler, selects, defaultIcon("select", "select")],
};
