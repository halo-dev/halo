import type { FormKitTypeDefinition } from "@formkit/core";

import {
  help,
  label,
  message,
  messages,
  outer,
  wrapper,
  type FormKitInputs,
} from "@formkit/inputs";
import { defineAsyncComponent } from "vue";
import { arraySection } from "./sections/index";
/**
 * Input definition for a array input.
 * @public
 */
export const array: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: outer(
    wrapper(label("$label"), help("$help"), arraySection()),
    messages(message("$message.value"))
  ),

  /**
   * The type of node, can be a list, group, or input.
   */
  type: "list",
  /**
   * An array of extra props to accept for this input.
   */
  props: [
    "min",
    "max",
    "removeControl",
    "addLabel",
    "addButton",
    "itemLabels",
    "emptyText",
  ],
  /**
   * Additional features that make this input work.
   */
  library: {
    ArrayInput: defineAsyncComponent(() => import("./ArrayInput.vue")),
  },
};

export type ArrayItemLabelType = "image" | "text" | "iconify" | "color";

export type ArrayItemLabel = {
  type: ArrayItemLabelType;
  label: string;
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    array: {
      type: "array";
      value?: Record<string, unknown>[];
      itemLabels?: {
        type: ArrayItemLabelType;
        label: string;
      }[];
    };
  }
}
