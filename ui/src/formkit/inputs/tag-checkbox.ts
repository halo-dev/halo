import { paginate } from "@/utils/paginate";
import type { FormKitNode, FormKitTypeDefinition } from "@formkit/core";
import {
  checkbox,
  checkboxes,
  defaultIcon,
  type FormKitInputs,
} from "@formkit/inputs";
import {
  coreApiClient,
  type Tag,
  type TagV1alpha1ApiListTagRequest,
} from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
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
