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
  type Policy,
  type PolicyV1alpha1ApiListPolicyRequest,
} from "@halo-dev/api-client";
import { defineAsyncComponent } from "vue";
import { select } from "./select";
import { SelectSection } from "./select/sections";

const PolicySettingsButton = defineAsyncComponent(
  () => import("./PolicySettingsButton.vue")
);

const PolicySettingsSuffix = createSection("PolicySettingsSuffix", () => ({
  $cmp: "PolicySettingsButton",
  props: {
    context: "$node.context",
  },
}));

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
  schema: outer(
    wrapper(
      label("$label"),
      inner(
        icon("prefix"),
        prefix(),
        SelectSection(),
        suffix(),
        PolicySettingsSuffix()
      )
    ),
    help("$help"),
    messages(message("$message.value"))
  ),
  forceTypeProp: "select",
  features: [optionsHandler],
  library: {
    ...select.library,
    PolicySettingsButton,
  },
  schemaMemoKey: "custom-attachment-policy-select",
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachmentPolicySelect: {
      type: "attachmentPolicySelect";
      value?: string;
    };
  }
}
