import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import {
  checkbox,
  checkboxes,
  defaultIcon,
  type FormKitInputs,
} from "@formkit/inputs";
import {
  coreApiClient,
  paginate,
  type Tag,
  type TagV1alpha1ApiListTagRequest,
} from "@halo-dev/api-client";
import { applyDataPermissionGuard } from "@/formkit/utils/permission";

const requiredPermissions = [
  "system:posts:view",
  "system:posts:manage",
  "uc:posts:manage",
];

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    if (!applyDataPermissionGuard(node, requiredPermissions)) {
      node.props.options = [];
      return;
    }

    const tags = await paginate<TagV1alpha1ApiListTagRequest, Tag>(
      (params) => coreApiClient.content.tag.listTag(params),
      {
        sort: ["metadata.creationTimestamp,desc"],
      }
    );

    node.props.options = tags.map((tag) => {
      return {
        value: tag.metadata.name,
        label: tag.spec.displayName,
      };
    });
  });
}

export const tagCheckbox: FormKitTypeDefinition = {
  ...checkbox,
  props: ["onValue", "offValue"],
  forceTypeProp: "checkbox",
  features: [
    optionsHandler,
    checkboxes,
    defaultIcon("decorator", "checkboxDecorator"),
  ],
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    tagCheckbox: {
      type: "tagCheckbox";
      value?: string[];
    };
  }
}
