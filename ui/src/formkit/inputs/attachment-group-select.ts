import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import type { FormKitInputs } from "@formkit/inputs";
import {
  createSection,
  help,
  icon,
  inner,
  label,
  message,
  messages,
  outer,
  prefix,
  suffix,
  wrapper,
} from "@formkit/inputs";
import {
  coreApiClient,
  paginate,
  type Group,
  type GroupV1alpha1ApiListGroupRequest,
} from "@halo-dev/api-client";
import { defineAsyncComponent } from "vue";
import { select } from "./select";
import { SelectSection } from "./select/sections";

const GroupSettingsButton = defineAsyncComponent(
  () => import("./GroupSettingsButton.vue")
);

const GroupSettingsSuffix = createSection("GroupSettingsSuffix", () => ({
  $cmp: "GroupSettingsButton",
  props: {
    context: "$node.context",
  },
}));

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
  schema: outer(
    wrapper(
      label("$label"),
      inner(
        icon("prefix"),
        prefix(),
        SelectSection(),
        suffix(),
        GroupSettingsSuffix()
      )
    ),
    help("$help"),
    messages(message("$message.value"))
  ),
  forceTypeProp: "select",
  features: [optionsHandler],
  library: {
    ...select.library,
    GroupSettingsButton,
  },
  schemaMemoKey: "custom-attachment-group-select",
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachmentGroupSelect: {
      type: "attachmentGroupSelect";
      value?: string;
    };
  }
}
