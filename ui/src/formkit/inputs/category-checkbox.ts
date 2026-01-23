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
  type Category,
  type CategoryV1alpha1ApiListCategoryRequest,
} from "@halo-dev/api-client";

function optionsHandler(node: FormKitNode) {
  node.on("created", async () => {
    const categories = await paginate<
      CategoryV1alpha1ApiListCategoryRequest,
      Category
    >((params) => coreApiClient.content.category.listCategory(params), {
      sort: ["metadata.creationTimestamp,desc"],
    });

    node.props.options = categories.map((category) => {
      return {
        value: category.metadata.name,
        label: category.spec.displayName,
      };
    });
  });
}

export const categoryCheckbox: FormKitTypeDefinition = {
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
    categoryCheckbox: {
      type: "categoryCheckbox";
      value?: string[];
    };
  }
}
